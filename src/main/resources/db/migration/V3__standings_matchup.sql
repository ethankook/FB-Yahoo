-- =========================================
-- V3__standings_matchup.sql
-- Add standings columns to team, create matchup tables
-- =========================================

-- =========================
-- STANDINGS (columns on team)
-- =========================

ALTER TABLE team ADD COLUMN standing_rank INTEGER;
ALTER TABLE team ADD COLUMN standing_wins INTEGER;
ALTER TABLE team ADD COLUMN standing_losses INTEGER;
ALTER TABLE team ADD COLUMN standing_ties INTEGER;
ALTER TABLE team ADD COLUMN standing_pct NUMERIC(5,3);
ALTER TABLE team ADD COLUMN standing_games_back NUMERIC(5,1);
ALTER TABLE team ADD COLUMN standing_points_for NUMERIC(8,1);
ALTER TABLE team ADD COLUMN standing_points_against NUMERIC(8,1);

-- =========================
-- MATCHUP
-- =========================

CREATE TABLE matchup (
    id          BIGSERIAL PRIMARY KEY,
    league_key  TEXT NOT NULL REFERENCES league(league_key) ON DELETE CASCADE,
    week        INTEGER NOT NULL,
    team1_key   TEXT NOT NULL REFERENCES team(team_key) ON DELETE CASCADE,
    team2_key   TEXT NOT NULL REFERENCES team(team_key) ON DELETE CASCADE,
    is_tied     BOOLEAN,
    winner_team_key TEXT REFERENCES team(team_key) ON DELETE SET NULL,
    UNIQUE(league_key, week, team1_key)
);

CREATE INDEX idx_matchup_league_week ON matchup (league_key, week);

-- =========================
-- MATCHUP STAT
-- =========================

CREATE TABLE matchup_stat (
    id          BIGSERIAL PRIMARY KEY,
    matchup_id  BIGINT NOT NULL REFERENCES matchup(id) ON DELETE CASCADE,
    stat_id     INTEGER NOT NULL,
    stat_name   TEXT NOT NULL,
    team1_value NUMERIC(8,3),
    team2_value NUMERIC(8,3),
    is_tied     BOOLEAN,
    winner_team_key TEXT REFERENCES team(team_key) ON DELETE SET NULL
);

CREATE INDEX idx_matchup_stat_matchup_id ON matchup_stat (matchup_id);
