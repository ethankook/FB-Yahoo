package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.StandingsIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingest/standings")
public class StandingsIngestionController {

    private final StandingsIngestionService standingsIngestionService;

    public StandingsIngestionController(StandingsIngestionService standingsIngestionService) {
        this.standingsIngestionService = standingsIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllStandings() {
        standingsIngestionService.ingestAllStandings();
        return "redirect:/success";
    }

    @GetMapping("/{leagueKey}")
    public String ingestStandings(@PathVariable String leagueKey) {
        standingsIngestionService.ingestStandings(leagueKey);
        return "redirect:/success";
    }
}
