package com.example.fbyahoo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class LeagueRosteredPlayerId implements Serializable {

    @Column(name = "league_key")
    private String leagueKey;

    @Column(name = "player_id")
    private String playerId;
}