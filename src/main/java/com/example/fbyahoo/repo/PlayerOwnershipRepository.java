package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.PlayerOwnership;
import com.example.fbyahoo.model.PlayerOwnershipId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerOwnershipRepository extends JpaRepository<PlayerOwnership, PlayerOwnershipId> {

    Optional<PlayerOwnership> findByIdPlayerIdAndIdSeason(String playerId, Integer season);

    List<PlayerOwnership> findByIdSeason(Integer season);

    @Query("""
        SELECT o
        FROM PlayerOwnership o
        WHERE o.id.season = :season
          AND o.id.playerId NOT IN (
              SELECT lrp.id.playerId
              FROM LeagueRosteredPlayer lrp
              WHERE lrp.id.leagueKey = :leagueKey
          )
        ORDER BY COALESCE(o.deltaWeek, -999999) DESC
        """)
    List<PlayerOwnership> findAvailableOrderedByDeltaWeekDesc(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            Pageable pageable
    );

    @Query("""
        SELECT o
        FROM PlayerOwnership o
        WHERE o.id.season = :season
          AND o.id.playerId NOT IN (
              SELECT lrp.id.playerId
              FROM LeagueRosteredPlayer lrp
              WHERE lrp.id.leagueKey = :leagueKey
          )
        ORDER BY o.percentOwned DESC
        """)
    List<PlayerOwnership> findAvailableOrderedByPercentOwnedDesc(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            Pageable pageable
    );
}