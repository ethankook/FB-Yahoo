package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.PlayerStatsIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingest/stats")
public class PlayerStatsIngestionController {

    private final PlayerStatsIngestionService playerStatsIngestionService;

    public PlayerStatsIngestionController(PlayerStatsIngestionService playerStatsIngestionService) {
        this.playerStatsIngestionService = playerStatsIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllPlayerStats() {
        playerStatsIngestionService.ingestSeasonAveragesForAllPlayers();
        return "redirect:/success";
    }
}
