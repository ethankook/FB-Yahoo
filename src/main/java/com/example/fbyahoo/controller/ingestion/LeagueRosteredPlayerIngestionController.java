package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.LeagueRosteredPlayerIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingest/lrp")
public class LeagueRosteredPlayerIngestionController {

    private final LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService;

    public LeagueRosteredPlayerIngestionController(LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService) {
        this.leagueRosteredPlayerIngestionService = leagueRosteredPlayerIngestionService;
    }

    @GetMapping("/{leagueKey}")
    public String ingestLeagueRosteredPlayers(@PathVariable String leagueKey) {
        leagueRosteredPlayerIngestionService.syncLeagueRosteredPlayers(leagueKey);
        return "redirect:/success";
    }

    @GetMapping("/all")
    public String ingestAllLeaguesRosteredPlayers() {
        leagueRosteredPlayerIngestionService.syncAllLeaguesRosteredPlayers();
        return "redirect:/success";
    }
}
