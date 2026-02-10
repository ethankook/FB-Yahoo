package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.PlayerStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerStatsRepository extends JpaRepository<PlayerStats, String> {

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.ptsPg DESC
            """)
    List<PlayerStats> topAvailableByPtsPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.rebPg DESC
            """)
    List<PlayerStats> topAvailableByRebPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.astPg DESC
            """)
    List<PlayerStats> topAvailableByAstPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.stlPg DESC
            """)
    List<PlayerStats> topAvailableByStlPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.blkPg DESC
            """)
    List<PlayerStats> topAvailableByBlkPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.tovPg ASC
            """)
    List<PlayerStats> topAvailableByLowTovPg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.fgPct DESC
            """)
    List<PlayerStats> topAvailableByFgPct(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.ftPct DESC
            """)
    List<PlayerStats> topAvailableByFtPct(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );

    @Query("""
            SELECT ps
            FROM PlayerStats ps
            WHERE ps.id NOT IN (
                SELECT lrp.id.playerId
                FROM LeagueRosteredPlayer lrp
                WHERE lrp.id.leagueKey = :leagueKey
            )
            ORDER BY ps.fg3MadePg DESC
            """)
    List<PlayerStats> topAvailableByFg3MadePg(
            @Param("leagueKey") String leagueKey,
            Pageable pageable
    );
}