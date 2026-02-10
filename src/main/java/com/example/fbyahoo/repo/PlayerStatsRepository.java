package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.model.PlayerStatsId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, PlayerStatsId> {

    Optional<PlayerStats> findByIdPlayerIdAndIdSeasonAndIdWindowType(String playerId, Integer season, String windowType);

    List<PlayerStats> findByIdSeasonAndIdWindowType(Integer season, String windowType);

    // -----------------------------
    // Top available by stat (per-game)
    // windowType = 'season' OR 'week'
    // leagueKey determines availability via league_rostered_player snapshot
    // -----------------------------

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id.season = :season
              AND ps.id.windowType = :windowType
              AND ps.id.playerId NOT IN (
                  SELECT lrp.id.playerId
                  FROM LeagueRosteredPlayer lrp
                  WHERE lrp.id.leagueKey = :leagueKey
              )
            ORDER BY ps.ptsPg DESC
            """)
    List<PlayerStats> topAvailableByPtsPg(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            @Param("windowType") String windowType,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id.season = :season
              AND ps.id.windowType = :windowType
              AND ps.id.playerId NOT IN (
                  SELECT lrp.id.playerId
                  FROM LeagueRosteredPlayer lrp
                  WHERE lrp.id.leagueKey = :leagueKey
              )
            ORDER BY ps.rebPg DESC
            """)
    List<PlayerStats> topAvailableByRebPg(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            @Param("windowType") String windowType,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id.season = :season
              AND ps.id.windowType = :windowType
              AND ps.id.playerId NOT IN (
                  SELECT lrp.id.playerId
                  FROM LeagueRosteredPlayer lrp
                  WHERE lrp.id.leagueKey = :leagueKey
              )
            ORDER BY ps.astPg DESC
            """)
    List<PlayerStats> topAvailableByAstPg(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            @Param("windowType") String windowType,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id.season = :season
              AND ps.id.windowType = :windowType
              AND ps.id.playerId NOT IN (
                  SELECT lrp.id.playerId
                  FROM LeagueRosteredPlayer lrp
                  WHERE lrp.id.leagueKey = :leagueKey
              )
            ORDER BY ps.stlPg DESC
            """)
    List<PlayerStats> topAvailableByStlPg(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            @Param("windowType") String windowType,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id.season = :season
              AND ps.id.windowType = :windowType
              AND ps.id.playerId NOT IN (
                  SELECT lrp.id.playerId
                  FROM LeagueRosteredPlayer lrp
                  WHERE lrp.id.leagueKey = :leagueKey
              )
            ORDER BY ps.blkPg DESC
            """)
    List<PlayerStats> topAvailableByBlkPg(
            @Param("leagueKey") String leagueKey,
            @Param("season") Integer season,
            @Param("windowType") String windowType,
            Pageable pageable
    );
}