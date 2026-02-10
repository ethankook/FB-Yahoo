package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Team {

    @Id
    @Column(name = "team_key", nullable = false)
    private String teamKey;

    @Column(name = "team_id")
    private String teamId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_key", nullable = false, referencedColumnName = "league_key")
    private League league;

    @Column(name = "name")
    private String name;

    @Column(name = "is_owned_by_current_login")
    private Boolean isOwnedByCurrentLogin;

    @Column(name = "url")
    private String url;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "waiver_priority")
    private Integer waiverPriority;

    @Column(name = "number_of_moves")
    private Integer numberOfMoves;

    @Column(name = "number_of_trades")
    private Integer numberOfTrades;

    @Column(name = "roster_adds_coverage_type")
    private String rosterAddsCoverageType;

    @Column(name = "roster_adds_coverage_value")
    private Integer rosterAddsCoverageValue;

    @Column(name = "roster_adds_value")
    private Integer rosterAddsValue;

    @Column(name = "clinched_playoffs")
    private Boolean clinchedPlayoffs;

    @Column(name = "league_scoring_type")
    private String leagueScoringType;

    @Column(name = "draft_position")
    private Integer draftPosition;

    @Column(name = "has_draft_grade")
    private Boolean hasDraftGrade;

    // Manager flattened
    @Column(name = "manager_id")
    private String managerId;

    @Column(name = "manager_nickname")
    private String managerNickname;

    @Column(name = "manager_guid")
    private String managerGuid;

    @Column(name = "manager_is_commissioner")
    private Boolean managerIsCommissioner;

    @Column(name = "manager_is_current_login")
    private Boolean managerIsCurrentLogin;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(name = "manager_image_url")
    private String managerImageUrl;

    @Column(name = "manager_felo_score")
    private Integer managerFeloScore;

    @Column(name = "manager_felo_tier")
    private String managerFeloTier;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}