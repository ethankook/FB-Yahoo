package com.example.fbyahoo.controller.api;

import com.example.fbyahoo.dto.api.*;
import com.example.fbyahoo.model.League;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.service.LeagueReadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
public class LeagueApiController {

    private final LeagueRepository leagueRepository;
    private final LeagueReadService leagueReadService;

    public LeagueApiController(
            LeagueRepository leagueRepository,
            LeagueReadService leagueReadService
    ) {
        this.leagueRepository = leagueRepository;
        this.leagueReadService = leagueReadService;
    }

    @GetMapping
    public List<LeagueSummaryDto> listLeagues() {
        return leagueReadService.listLeagues();
    }

    @GetMapping("/{leagueKey}")
    public ResponseEntity<LeagueDetailDto> getLeague(@PathVariable String leagueKey) {
        if (leagueRepository.findById(leagueKey).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        LeagueDetailDto detail = leagueReadService.getLeagueDetail(leagueKey);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{leagueKey}/roster")
    public ResponseEntity<List<RosterPlayerDto>> getRoster(@PathVariable String leagueKey) {
        return ResponseEntity.ok(leagueReadService.getRoster(leagueKey));
    }

    @GetMapping("/{leagueKey}/available")
    public List<AvailablePlayerDto> getAvailablePlayers(
            @PathVariable String leagueKey,
            @RequestParam(defaultValue = "pts") String category,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return leagueReadService.getAvailablePlayers(leagueKey, category, limit);
    }

    @GetMapping("/{leagueKey}/standings")
    public ResponseEntity<List<StandingDto>> getStandings(@PathVariable String leagueKey) {
        if (leagueRepository.findById(leagueKey).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leagueReadService.getStandings(leagueKey));
    }

    @GetMapping("/{leagueKey}/matchup")
    public ResponseEntity<MatchupDto> getMatchup(
            @PathVariable String leagueKey,
            @RequestParam(required = false) Integer week
    ) {
        League league = leagueRepository.findById(leagueKey).orElse(null);
        if (league == null) {
            return ResponseEntity.notFound().build();
        }

        int targetWeek = week != null
                ? week
                : (league.getMatchupWeek() != null
                    ? league.getMatchupWeek()
                    : (league.getCurrentWeek() != null ? league.getCurrentWeek() : 1));
        return ResponseEntity.ok(leagueReadService.getMatchup(leagueKey, targetWeek));
    }

    @GetMapping("/{leagueKey}/insights")
    public ResponseEntity<InsightsDto> getInsights(@PathVariable String leagueKey) {
        if (leagueRepository.findById(leagueKey).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(leagueReadService.getInsights(leagueKey));
    }
}
