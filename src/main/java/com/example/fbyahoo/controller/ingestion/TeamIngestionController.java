package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.LeagueIngestionService;
import com.example.fbyahoo.service.ingestion.TeamIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/ingest/team")
public class TeamIngestionController {

    private final TeamIngestionService teamIngestionService;

    public TeamIngestionController(TeamIngestionService teamIngestionService) {
        this.teamIngestionService = teamIngestionService;
    }

    @GetMapping("/{leagueKey}")
    public String ingestAllLeagues(@PathVariable String leagueKey) throws IOException {
        teamIngestionService.usurpLeague(leagueKey);
        return "redirect:/success";

    }

}