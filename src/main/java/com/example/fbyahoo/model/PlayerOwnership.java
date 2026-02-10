package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "player_ownership")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerOwnership {

    @EmbeddedId
    private PlayerOwnershipId id;

    @MapsId("playerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "week", nullable = false)
    private Integer week;

    @Column(name = "percent_owned", nullable = false)
    private Integer percentOwned;

    @Column(name = "delta_week")
    private Integer deltaWeek;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}