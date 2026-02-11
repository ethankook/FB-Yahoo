package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.TeamRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StandingsIngestionServiceTest {

    @Test
    void ingestStandings_updatesTeam() {
        String json = """
                {
                  "fantasy_content": {
                    "league": [
                      {},
                      {
                        "standings": [
                          {
                            "teams": {
                              "count": 1,
                              "0": {
                                "team": [
                                  [ { "team_key": "t1" } ],
                                  { "team_standings": {
                                      "rank": "1",
                                      "outcome_totals": {
                                        "wins": "10",
                                        "losses": "5",
                                        "ties": "0",
                                        "percentage": "0.667"
                                      },
                                      "games_back": "0",
                                      "points_for": "1000",
                                      "points_against": "900"
                                  } }
                                ]
                              }
                            }
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        WebClient webClient = WebClientTestUtil.webClientForJson(json);
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.getValidAccessToken()).thenReturn("token");

        TeamRepository teamRepository = mock(TeamRepository.class);
        Team team = new Team();
        team.setTeamKey("t1");
        when(teamRepository.findById("t1")).thenReturn(Optional.of(team));

        StandingsIngestionService service = new StandingsIngestionService(
                webClient,
                tokenService,
                new ObjectMapper(),
                mock(LeagueRepository.class),
                teamRepository
        );

        service.ingestStandings("l1");

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        Team saved = captor.getValue();

        assertEquals(Integer.valueOf(1), saved.getStandingRank());
        assertEquals(Integer.valueOf(10), saved.getStandingWins());
    }
}
