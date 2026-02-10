package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.PlayerIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingest/player")
public class PlayerIngestionController {

    private final PlayerIngestionService playerIngestionService;

    public PlayerIngestionController(PlayerIngestionService playerIngestionService) {
        this.playerIngestionService = playerIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllPlayers() {
        playerIngestionService.usurpAllPlayers();
        return "redirect:/success";
    }
}
