package com.example.fbyahoo.repo;

import com.example.fbyahoo.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, String> {

    List<Team> findByLeagueKey(String leagueKey);

    boolean existsByLeagueKeyAndTeamId(String leagueKey, String teamId);
}