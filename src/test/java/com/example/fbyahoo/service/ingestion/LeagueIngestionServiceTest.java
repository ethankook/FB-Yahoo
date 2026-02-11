package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.repo.LeagueRepository;
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
class LeagueIngestionServiceTest {

    @Mock
    LeagueRepository leagueRepository;

    @Mock
    WebClient fantasyClient;

    @Mock
    TokenService tokenService;

    private ObjectMapper objectMapper;
    private LeagueIngestionService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new LeagueIngestionService(leagueRepository, fantasyClient, tokenService, objectMapper);
    }

    @Test
    void parseLeague_setsIngestedAtWhenMissing() throws Exception {
        String json = """
                {
                  "league_key": "123.l.456",
                  "league_id": "456",
                  "name": "Test League",
                  "game_code": "nba",
                  "season": "2025",
                  "num_teams": "10",
                  "current_date": "2025-01-01"
                }
                """;
        JsonNode node = objectMapper.readTree(json);

        when(leagueRepository.findById("123.l.456")).thenReturn(Optional.empty());

        League league = service.parseLeague(node);

        assertEquals("123.l.456", league.getLeagueKey());
        assertEquals(Integer.valueOf(2025), league.getSeason());
        assertNotNull(league.getIngestedAt());
        assertNotNull(league.getUpdatedAt());
    }

    @Test
    void parseLeague_throwsWhenLeagueKeyMissing() throws Exception {
        JsonNode node = objectMapper.readTree("{}");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.parseLeague(node));

        assertTrue(ex.getMessage().contains("league_key"));
    }
}
