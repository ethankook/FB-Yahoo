package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueRepository extends JpaRepository<League, String> {

    List<League> findByGameCodeAndSeason(String gameCode, Integer season);

    League findByLeagueKey(String leagueKey);

    List<League> findBySeason(Integer season);
}