package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "player_stats")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerStats {

    @Id
    @Column(name = "player_id", nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "games_played", nullable = false)
    private Integer gamesPlayed;

    @Column(name = "pts_pg")
    private BigDecimal ptsPg;

    @Column(name = "reb_pg")
    private BigDecimal rebPg;

    @Column(name = "ast_pg")
    private BigDecimal astPg;

    @Column(name = "stl_pg")
    private BigDecimal stlPg;

    @Column(name = "blk_pg")
    private BigDecimal blkPg;

    @Column(name = "tov_pg")
    private BigDecimal tovPg;

    @Column(name = "fg_made_pg")
    private BigDecimal fgMadePg;

    @Column(name = "fg_att_pg")
    private BigDecimal fgAttPg;

    @Column(name = "ft_made_pg")
    private BigDecimal ftMadePg;

    @Column(name = "ft_att_pg")
    private BigDecimal ftAttPg;

    @Column(name = "fg3_made_pg")
    private BigDecimal fg3MadePg;

    @Column(name = "fg3_att_pg")
    private BigDecimal fg3AttPg;

    @Column(name = "fg_pct")
    private BigDecimal fgPct;

    @Column(name = "ft_pct")
    private BigDecimal ftPct;

    @Column(name = "fg3_pct")
    private BigDecimal fg3Pct;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}