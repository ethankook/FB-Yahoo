package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Matchup;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.MatchupRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MatchupIngestionServiceTest {

    @Test
    void ingestMatchups_savesMatchupWithStats() {
        String json = """
                {
                  "fantasy_content": {
                    "league": [
                      {},
                      {
                        "scoreboard": {
                          "0": {
                            "matchups": {
                              "count": 1,
                              "0": {
                                "matchup": {
                                  "0": {
                                    "teams": {
                                      "0": { "team": [ [ { "team_key": "t1" } ], { "team_stats": { "stats": [ { "stat": { "stat_id": "12", "value": "100" } } ] } } ] },
                                      "1": { "team": [ [ { "team_key": "t2" } ], { "team_stats": { "stats": [ { "stat": { "stat_id": "12", "value": "90" } } ] } } ] }
                                    }
                                  },
                                  "stat_winners": [
                                    { "stat_winner": { "stat_id": "12", "is_tied": "0", "winner_team_key": "t1" } }
                                  ],
                                  "is_tied": "0",
                                  "winner_team_key": "t1"
                                }
                              }
                            }
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

        MatchupRepository matchupRepository = mock(MatchupRepository.class);
        when(matchupRepository.findByLeagueKeyAndWeekAndTeam1Key("l1", 1, "t1")).thenReturn(java.util.Optional.empty());

        MatchupIngestionService service = new MatchupIngestionService(
                webClient,
                tokenService,
                new ObjectMapper(),
                mock(LeagueRepository.class),
                matchupRepository
        );

        service.ingestMatchups("l1", 1);

        ArgumentCaptor<Matchup> captor = ArgumentCaptor.forClass(Matchup.class);
        verify(matchupRepository).save(captor.capture());
        Matchup saved = captor.getValue();

        assertEquals("l1", saved.getLeagueKey());
        assertEquals(1, saved.getWeek());
        assertEquals("t1", saved.getTeam1Key());
        assertEquals("t2", saved.getTeam2Key());
        assertEquals(1, saved.getStats().size());
    }
}
