package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.Matchup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchupRepository extends JpaRepository<Matchup, Long> {

    List<Matchup> findByLeagueKeyAndWeek(String leagueKey, Integer week);

    Optional<Matchup> findByLeagueKeyAndWeekAndTeam1Key(String leagueKey, Integer week, String team1Key);
}
