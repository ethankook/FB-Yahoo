package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "matchup_stat")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MatchupStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matchup_id", nullable = false)
    private Matchup matchup;

    @Column(name = "stat_id", nullable = false)
    private Integer statId;

    @Column(name = "stat_name", nullable = false)
    private String statName;

    @Column(name = "team1_value")
    private BigDecimal team1Value;

    @Column(name = "team2_value")
    private BigDecimal team2Value;

    @Column(name = "is_tied")
    private Boolean isTied;

    @Column(name = "winner_team_key")
    private String winnerTeamKey;
}
