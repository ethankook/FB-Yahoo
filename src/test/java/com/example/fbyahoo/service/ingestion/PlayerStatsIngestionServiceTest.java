package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.repo.PlayerRepository;
import com.example.fbyahoo.repo.PlayerStatsRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PlayerStatsIngestionServiceTest {

    @Test
    void ingestSeasonAveragesForAllPlayers_savesStats() {
        String json = """
                {
                  "fantasy_content": {
                    "game": [
                      {},
                      {
                        "players": {
                          "0": {
                            "player": [
                              [ { "player_id": "p1" } ],
                              { "player_stats": { "stats": [
                                { "stat": { "stat_id": "0", "value": "10" } },
                                { "stat": { "stat_id": "2", "value": "2025" } },
                                { "stat": { "stat_id": "12", "value": "100" } },
                                { "stat": { "stat_id": "15", "value": "50" } },
                                { "stat": { "stat_id": "16", "value": "30" } },
                                { "stat": { "stat_id": "17", "value": "5" } },
                                { "stat": { "stat_id": "18", "value": "3" } },
                                { "stat": { "stat_id": "19", "value": "20" } },
                                { "stat": { "stat_id": "5", "value": "0.45" } },
                                { "stat": { "stat_id": "8", "value": "0.80" } },
                                { "stat": { "stat_id": "11", "value": "0.35" } },
                                { "stat": { "stat_id": "3", "value": "100" } },
                                { "stat": { "stat_id": "4", "value": "45" } },
                                { "stat": { "stat_id": "6", "value": "50" } },
                                { "stat": { "stat_id": "7", "value": "40" } },
                                { "stat": { "stat_id": "9", "value": "30" } },
                                { "stat": { "stat_id": "10", "value": "10" } }
                              ] } }
                            ]
                          }
                        }
                      }
                    ]
                  }
                }
                """;

        WebClient webClient = WebClientTestUtil.webClientForJson(json);
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.getValidAccessToken()).thenReturn("token");

        PlayerRepository playerRepository = mock(PlayerRepository.class);
        PlayerStatsRepository statsRepository = mock(PlayerStatsRepository.class);

        Player player = new Player();
        player.setPlayerId("p1");
        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));
        when(statsRepository.findById("p1")).thenReturn(Optional.empty());

        PlayerStatsIngestionService service = new PlayerStatsIngestionService(
                statsRepository,
                playerRepository,
                webClient,
                tokenService,
                new ObjectMapper()
        );

        service.ingestSeasonAveragesForAllPlayers();

        ArgumentCaptor<PlayerStats> captor = ArgumentCaptor.forClass(PlayerStats.class);
        verify(statsRepository).save(captor.capture());
        PlayerStats saved = captor.getValue();

        assertEquals(Integer.valueOf(10), saved.getGamesPlayed());
        assertEquals(Integer.valueOf(2025), saved.getSeason());
    }
}
