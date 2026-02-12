package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerOwnership;
import com.example.fbyahoo.repo.PlayerOwnershipRepository;
import com.example.fbyahoo.repo.PlayerRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PlayerOwnershipIngestionServiceTest {

    @Test
    void ingestAllPlayerOwnership_savesOwnership() {
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
                              { "percent_owned": [ { "week": "1" }, { "value": "25" }, { "delta": "3" } ] }
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
        PlayerOwnershipRepository ownershipRepository = mock(PlayerOwnershipRepository.class);

        Player player = new Player();
        player.setPlayerId("p1");
        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));
        when(ownershipRepository.findById(any())).thenReturn(Optional.empty());

        PlayerOwnershipIngestionService service = new PlayerOwnershipIngestionService(
                ownershipRepository,
                playerRepository,
                webClient,
                tokenService,
                new ObjectMapper()
        );

        service.ingestAllPlayerOwnership();

        ArgumentCaptor<PlayerOwnership> captor = ArgumentCaptor.forClass(PlayerOwnership.class);
        verify(ownershipRepository).save(captor.capture());
        PlayerOwnership saved = captor.getValue();

        assertEquals(Integer.valueOf(1), saved.getWeek());
        assertEquals(Integer.valueOf(25), saved.getPercentOwned());
        assertEquals(Integer.valueOf(3), saved.getDeltaWeek());
    }
}
