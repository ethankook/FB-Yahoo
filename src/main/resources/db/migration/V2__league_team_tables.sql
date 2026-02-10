-- =========================================
-- V1__init.sql
-- Initial schema for FB Helper (Yahoo-first)
-- PostgreSQL 13+
-- =========================================

-- =========================
-- LEAGUE
-- =========================

CREATE TABLE league
(
    league_key                TEXT PRIMARY KEY, -- "466.l.42086"
    league_id                 TEXT,             -- "42086"

    name                      TEXT,
    game_code                 TEXT,             -- "nba"
    season                    INTEGER,          -- 2025

    url                       TEXT,
    logo_url                  TEXT,

    draft_status              TEXT,
    num_teams                 INTEGER,
    edit_key                  DATE,
    weekly_deadline           TEXT,
    roster_type               TEXT,

    league_update_timestamp   BIGINT,
    scoring_type              TEXT,
    league_type               TEXT,

    renew                     TEXT,
    renewed                   TEXT,
    felo_tier                 TEXT,

    is_highscore              BOOLEAN,
    matchup_week              INTEGER,

    iris_group_chat_id        TEXT,
    short_invitation_url      TEXT,
    allow_add_to_dl_extra_pos INTEGER,

    is_pro_league             BOOLEAN,
    is_cash_league            BOOLEAN,
    is_plus_league            BOOLEAN,

    current_week              INTEGER,
    start_week                INTEGER,
    start_date                DATE,
    end_week                  INTEGER,
    end_date                  DATE,
    current_date_             DATE,

    ingested_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_league_game_code ON league (game_code);
CREATE INDEX idx_league_season ON league (season);

-- =========================
-- TEAM
-- =========================

CREATE TABLE team
(
    team_key                   TEXT PRIMARY KEY, -- "466.l.42086.t.3"
    team_id                    TEXT,             -- "3"

    league_key                 TEXT        NOT NULL REFERENCES league (league_key) ON DELETE CASCADE,

    name                       TEXT,

    is_owned_by_current_login  BOOLEAN,

    url                        TEXT,
    logo_url                   TEXT,

    waiver_priority            INTEGER,
    number_of_moves            INTEGER,
    number_of_trades           INTEGER,

    roster_adds_coverage_type  TEXT,
    roster_adds_coverage_value INTEGER,
    roster_adds_value          INTEGER,

    clinched_playoffs          BOOLEAN,
    league_scoring_type        TEXT,

    draft_position             INTEGER,
    has_draft_grade            BOOLEAN,

    -- Manager (flattened, 1 per team in Yahoo)
    manager_id                 TEXT,
    manager_nickname           TEXT,
    manager_guid               TEXT,
    manager_is_commissioner    BOOLEAN,
    manager_is_current_login   BOOLEAN,
    manager_email              TEXT,
    manager_image_url          TEXT,
    manager_felo_score         INTEGER,
    manager_felo_tier          TEXT,

    ingested_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_team_league_key ON team (league_key);

-- =========================
-- PLAYER (GLOBAL)
-- =========================

CREATE TABLE player
(
    player_id                TEXT PRIMARY KEY,            -- "5009"
    player_key               TEXT        NOT NULL UNIQUE, -- "466.p.5009"

    name_full                TEXT,
    name_first               TEXT,
    name_last                TEXT,
    name_ascii_first         TEXT,
    name_ascii_last          TEXT,

    url                      TEXT,

    status                   TEXT,
    status_full              TEXT,
    injury_note              TEXT,

    editorial_player_key     TEXT,
    editorial_team_key       TEXT,
    editorial_team_full_name TEXT,
    editorial_team_abbr      TEXT,
    editorial_team_url       TEXT,

    uniform_number           TEXT,
    display_position         TEXT,

    headshot_url             TEXT,
    headshot_size            TEXT,
    image_url                TEXT,

    is_undroppable           BOOLEAN,
    position_type            TEXT,

    eligible_positions       TEXT[]      NOT NULL DEFAULT '{}',

    ingested_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Fast filtering by position (PG, SG, F, etc.)
CREATE INDEX idx_player_eligible_positions_gin
    ON player USING GIN (eligible_positions);

CREATE INDEX idx_player_name_full ON player (name_full);

-- =========================
-- PLAYER OWNERSHIP (1 row per player, updated weekly)
-- =========================

CREATE TABLE player_ownership
(
    player_id     TEXT        NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
    season        INTEGER     NOT NULL,
    week          INTEGER     NOT NULL,
    percent_owned INTEGER     NOT NULL CHECK (percent_owned BETWEEN 0 AND 100),
    delta_week    INTEGER,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (player_id, season)
);

CREATE INDEX idx_player_ownership_percent_owned
    ON player_ownership (percent_owned DESC);

CREATE INDEX idx_player_ownership_delta_week
    ON player_ownership (delta_week DESC);

-- =========================
-- League roster snapshot (league-specific availability)
-- =========================

CREATE TABLE league_rostered_player
(
    league_key TEXT        NOT NULL REFERENCES league (league_key) ON DELETE CASCADE,
    player_id  TEXT        NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
    team_key   TEXT        NOT NULL REFERENCES team (team_key) ON DELETE CASCADE,

    as_of      TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (league_key, player_id)
);

CREATE INDEX idx_lrp_league_key ON league_rostered_player (league_key, player_id);
CREATE INDEX idx_lrp_league_only ON league_rostered_player (league_key);
CREATE INDEX idx_lrp_team_key ON league_rostered_player (team_key);

-- =========================
-- Player stats (2 rows per player per season: season + latest week)
-- Stores PER-GAME averages (not totals)
-- =========================
CREATE TABLE player_stats
(
    player_id    TEXT        NOT NULL REFERENCES player (player_id) ON DELETE CASCADE,
    season       INTEGER     NOT NULL,

    games_played INTEGER     NOT NULL DEFAULT 0 CHECK (games_played >= 0),

    -- Per-game averages (use NUMERIC so you don’t lose precision)
    pts_pg       NUMERIC(8, 3),
    reb_pg       NUMERIC(8, 3),
    ast_pg       NUMERIC(8, 3),
    stl_pg       NUMERIC(8, 3),
    blk_pg       NUMERIC(8, 3),
    tov_pg       NUMERIC(8, 3),

    -- Shooting per-game (optional; some people prefer percentages only)
    fg_made_pg   NUMERIC(8, 3),
    fg_att_pg    NUMERIC(8, 3),
    ft_made_pg   NUMERIC(8, 3),
    ft_att_pg    NUMERIC(8, 3),
    fg3_made_pg  NUMERIC(8, 3),
    fg3_att_pg   NUMERIC(8, 3),

    -- Percentages (store if Yahoo provides; otherwise compute from made/att)
    fg_pct       NUMERIC(6, 3),
    ft_pct       NUMERIC(6, 3),
    fg3_pct      NUMERIC(6, 3),

    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Enforce meaning of week

    PRIMARY KEY (player_id, season)
);
