package com.example.fbyahoo.controller;

import com.example.fbyahoo.service.ingestion.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sync")
public class SyncController {

    private final LeagueIngestionService leagueIngestionService;
    private final TeamIngestionService teamIngestionService;
    private final PlayerIngestionService playerIngestionService;
    private final PlayerOwnershipIngestionService playerOwnershipIngestionService;
    private final LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService;
    private final PlayerStatsIngestionService playerStatsIngestionService;
    public SyncController(
            LeagueIngestionService leagueIngestionService,
            TeamIngestionService teamIngestionService,
            PlayerIngestionService playerIngestionService,
            PlayerOwnershipIngestionService playerOwnershipIngestionService,
            LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService,
            PlayerStatsIngestionService playerStatsIngestionService
    ) {
        this.leagueIngestionService = leagueIngestionService;
        this.teamIngestionService = teamIngestionService;
        this.playerIngestionService = playerIngestionService;
        this.playerOwnershipIngestionService = playerOwnershipIngestionService;
        this.leagueRosteredPlayerIngestionService = leagueRosteredPlayerIngestionService;
        this.playerStatsIngestionService = playerStatsIngestionService;
    }

    @GetMapping("/all")
    public String syncAll() {
        leagueIngestionService.ingestAllLeagues();
        teamIngestionService.ingestAllTeams();
        playerIngestionService.usurpAllPlayers();
        playerOwnershipIngestionService.ingestAllPlayerOwnership();
        leagueRosteredPlayerIngestionService.syncAllLeaguesRosteredPlayers();
        playerStatsIngestionService.ingestSeasonAveragesForAllPlayers();
        return "redirect:/success";
    }
}