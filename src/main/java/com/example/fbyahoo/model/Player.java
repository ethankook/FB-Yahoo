package com.example.fbyahoo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "player")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Player {

    @Id
    @Column(name = "player_id", nullable = false)
    private String playerId; // Yahoo player_id ("5009")

    @Column(name = "player_key", nullable = false, unique = true)
    private String playerKey; // "466.p.5009"

    @Column(name = "name_full")
    private String nameFull;

    @Column(name = "name_first")
    private String nameFirst;

    @Column(name = "name_last")
    private String nameLast;

    @Column(name = "name_ascii_first")
    private String nameAsciiFirst;

    @Column(name = "name_ascii_last")
    private String nameAsciiLast;

    @Column(name = "url")
    private String url;

    @Column(name = "status")
    private String status;

    @Column(name = "status_full")
    private String statusFull;

    @Column(name = "injury_note")
    private String injuryNote;

    @Column(name = "editorial_player_key")
    private String editorialPlayerKey;

    @Column(name = "editorial_team_key")
    private String editorialTeamKey;

    @Column(name = "editorial_team_full_name")
    private String editorialTeamFullName;

    @Column(name = "editorial_team_abbr")
    private String editorialTeamAbbr;

    @Column(name = "editorial_team_url")
    private String editorialTeamUrl;

    @Column(name = "uniform_number")
    private String uniformNumber;

    @Column(name = "display_position")
    private String displayPosition;

    @Column(name = "headshot_url")
    private String headshotUrl;

    @Column(name = "headshot_size")
    private String headshotSize;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_undroppable")
    private Boolean isUndroppable;

    @Column(name = "position_type")
    private String positionType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "eligible_positions", columnDefinition = "text[]", nullable = false)
    private String[] eligiblePositions;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}