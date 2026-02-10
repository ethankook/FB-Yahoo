package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.LeagueRosteredPlayer;
import com.example.fbyahoo.model.LeagueRosteredPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LeagueRosteredPlayerRepository extends JpaRepository<LeagueRosteredPlayer, LeagueRosteredPlayerId> {

    List<LeagueRosteredPlayer> findByIdLeagueKey(String leagueKey);

    boolean existsByIdLeagueKeyAndIdPlayerId(String leagueKey, String playerId);

    List<LeagueRosteredPlayer> findByTeam_TeamKey(String teamKey);

    @Modifying
    @Query("DELETE FROM LeagueRosteredPlayer lrp WHERE lrp.id.leagueKey = :leagueKey")
    int deleteByLeagueKey(@Param("leagueKey") String leagueKey);

    long deleteByLeague_LeagueKeyAndAsOfBefore(String leagueKey, Instant cutoff);
}