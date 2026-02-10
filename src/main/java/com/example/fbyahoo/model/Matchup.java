package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matchup")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Matchup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "league_key", nullable = false)
    private String leagueKey;

    @Column(name = "week", nullable = false)
    private Integer week;

    @Column(name = "team1_key", nullable = false)
    private String team1Key;

    @Column(name = "team2_key", nullable = false)
    private String team2Key;

    @Column(name = "is_tied")
    private Boolean isTied;

    @Column(name = "winner_team_key")
    private String winnerTeamKey;

    @OneToMany(mappedBy = "matchup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MatchupStat> stats = new ArrayList<>();
}
