package com.example.fbyahoo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PlayerOwnershipId implements Serializable {

    @Column(name = "player_id")
    private String playerId;

    @Column(name = "season")
    private Integer season;
}