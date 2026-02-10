package com.example.fbyahoo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PlayerStatsId implements Serializable {

    @Column(name = "player_id")
    private String playerId;

    @Column(name = "season")
    private Integer season;

    @Column(name = "window_type")
    private String windowType; // "season" or "week"
}