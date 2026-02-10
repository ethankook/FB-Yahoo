package com.example.fbyahoo.controller.ingestion;

import com.example.fbyahoo.service.ingestion.PlayerOwnershipIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingest/ownership")
public class PlayerOwnershipIngestionController {

    private final PlayerOwnershipIngestionService playerOwnershipIngestionService;

    public PlayerOwnershipIngestionController(PlayerOwnershipIngestionService playerOwnershipIngestionService) {
        this.playerOwnershipIngestionService = playerOwnershipIngestionService;
    }

    @GetMapping("/all")
    public String ingestAllPlayerOwnership() {
        playerOwnershipIngestionService.ingestAllPlayerOwnership();
        return "redirect:/success";
    }
}
