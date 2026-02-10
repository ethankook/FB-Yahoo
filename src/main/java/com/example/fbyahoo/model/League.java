package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "league")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class League {

    @Id
    @Column(name = "league_key", nullable = false)
    private String leagueKey;

    @Column(name = "league_id")
    private String leagueId;

    @Column(name = "name")
    private String name;

    @Column(name = "game_code")
    private String gameCode;

    @Column(name = "season")
    private Integer season;

    @Column(name = "url")
    private String url;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "draft_status")
    private String draftStatus;

    @Column(name = "num_teams")
    private Integer numTeams;

    @Column(name = "edit_key")
    private LocalDate editKey;

    @Column(name = "weekly_deadline")
    private String weeklyDeadline;

    @Column(name = "roster_type")
    private String rosterType;

    @Column(name = "league_update_timestamp")
    private Long leagueUpdateTimestamp;

    @Column(name = "scoring_type")
    private String scoringType;

    @Column(name = "league_type")
    private String leagueType;

    @Column(name = "renew")
    private String renew;

    @Column(name = "renewed")
    private String renewed;

    @Column(name = "felo_tier")
    private String feloTier;

    @Column(name = "is_highscore")
    private Boolean isHighscore;

    @Column(name = "matchup_week")
    private Integer matchupWeek;

    @Column(name = "iris_group_chat_id")
    private String irisGroupChatId;

    @Column(name = "short_invitation_url")
    private String shortInvitationUrl;

    @Column(name = "allow_add_to_dl_extra_pos")
    private Integer allowAddToDlExtraPos;

    @Column(name = "is_pro_league")
    private Boolean isProLeague;

    @Column(name = "is_cash_league")
    private Boolean isCashLeague;

    @Column(name = "is_plus_league")
    private Boolean isPlusLeague;

    @Column(name = "current_week")
    private Integer currentWeek;

    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_week")
    private Integer endWeek;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "current_date_")
    private LocalDate currentDate;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}