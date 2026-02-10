package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.LeagueIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/ingest/league")
public class LeagueIngestionController {

    private final LeagueIngestionService leagueIngestionService;

    public LeagueIngestionController(LeagueIngestionService leagueIngestionService) {
        this.leagueIngestionService = leagueIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllLeagues() throws IOException {
        leagueIngestionService.ingestAllLeagues();
        return "redirect:/success";

    }

}