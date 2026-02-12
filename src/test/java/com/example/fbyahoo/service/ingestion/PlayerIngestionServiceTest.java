package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.repo.PlayerRepository;
import com.example.fbyahoo.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerIngestionServiceTest {

    @Mock
    PlayerRepository playerRepository;

    @Mock
    WebClient fantasyClient;

    @Mock
    TokenService tokenService;

    private ObjectMapper objectMapper;
    private PlayerIngestionService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new PlayerIngestionService(playerRepository, fantasyClient, tokenService, objectMapper);
    }

    @Test
    void parsePlayer_populatesFields() throws Exception {
        String json = """
                [
                  {"player_key":"nba.p.123"},
                  {"player_id":"123"},
                  {"name":{"full":"Jane Doe","first":"Jane","last":"Doe","ascii_first":"Jane","ascii_last":"Doe"}},
                  {"eligible_positions":[{"position":"PG"},{"position":"SG"}]}
                ]
                """;
        JsonNode playerArray = objectMapper.readTree(json);

        when(playerRepository.findById("123")).thenReturn(Optional.empty());

        Player player = service.parsePlayer(playerArray);

        assertEquals("123", player.getPlayerId());
        assertEquals("nba.p.123", player.getPlayerKey());
        assertEquals("Jane Doe", player.getNameFull());
        assertArrayEquals(new String[]{"PG", "SG"}, player.getEligiblePositions());
        assertNotNull(player.getIngestedAt());
        assertNotNull(player.getUpdatedAt());
    }

    @Test
    void parsePlayer_throwsWhenMissingPlayerId() throws Exception {
        String json = """
                [
                  {"player_key":"nba.p.123"}
                ]
                """;
        JsonNode playerArray = objectMapper.readTree(json);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.parsePlayer(playerArray));

        assertTrue(ex.getMessage().contains("player_id"));
    }
}
