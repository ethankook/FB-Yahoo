package com.example.fbyahoo.controller.api;

import com.example.fbyahoo.config.CacheNames;
import com.example.fbyahoo.service.ingestion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncApiController {

    private static final Logger log = LoggerFactory.getLogger(SyncApiController.class);

    private final LeagueIngestionService leagueIngestionService;
    private final TeamIngestionService teamIngestionService;
    private final PlayerIngestionService playerIngestionService;
    private final PlayerOwnershipIngestionService playerOwnershipIngestionService;
    private final LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService;
    private final PlayerStatsIngestionService playerStatsIngestionService;
    private final StandingsIngestionService standingsIngestionService;
    private final MatchupIngestionService matchupIngestionService;

    public SyncApiController(
            LeagueIngestionService leagueIngestionService,
            TeamIngestionService teamIngestionService,
            PlayerIngestionService playerIngestionService,
            PlayerOwnershipIngestionService playerOwnershipIngestionService,
            LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService,
            PlayerStatsIngestionService playerStatsIngestionService,
            StandingsIngestionService standingsIngestionService,
            MatchupIngestionService matchupIngestionService
    ) {
        this.leagueIngestionService = leagueIngestionService;
        this.teamIngestionService = teamIngestionService;
        this.playerIngestionService = playerIngestionService;
        this.playerOwnershipIngestionService = playerOwnershipIngestionService;
        this.leagueRosteredPlayerIngestionService = leagueRosteredPlayerIngestionService;
        this.playerStatsIngestionService = playerStatsIngestionService;
        this.standingsIngestionService = standingsIngestionService;
        this.matchupIngestionService = matchupIngestionService;
    }

    @PostMapping
    @CacheEvict(
            cacheNames = {
                    CacheNames.LEAGUE_LIST,
                    CacheNames.LEAGUE_DETAIL,
                    CacheNames.LEAGUE_ROSTER,
                    CacheNames.LEAGUE_AVAILABLE,
                    CacheNames.LEAGUE_STANDINGS,
                    CacheNames.LEAGUE_MATCHUP,
                    CacheNames.LEAGUE_INSIGHTS
            },
            allEntries = true
    )
    public ResponseEntity<Map<String, String>> sync() {
        log.info("API sync started");
        leagueIngestionService.ingestAllLeagues();
        teamIngestionService.ingestAllTeams();
        playerIngestionService.usurpAllPlayers();
        playerOwnershipIngestionService.ingestAllPlayerOwnership();
        leagueRosteredPlayerIngestionService.syncAllLeaguesRosteredPlayers();
        playerStatsIngestionService.ingestSeasonAveragesForAllPlayers();
        standingsIngestionService.ingestAllStandings();
        matchupIngestionService.ingestAllMatchups();
        log.info("API sync completed");
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
