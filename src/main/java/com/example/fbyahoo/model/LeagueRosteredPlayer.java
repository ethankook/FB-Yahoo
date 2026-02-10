package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "league_rostered_player")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LeagueRosteredPlayer {

    @EmbeddedId
    private LeagueRosteredPlayerId id;

    @MapsId("leagueKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_key", nullable = false)
    private League league;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_key", nullable = false)
    private Team team;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;
}