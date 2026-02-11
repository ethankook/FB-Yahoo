package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.TeamRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TeamIngestionServiceTest {

    @Test
    void usurpLeague_savesTeamFromPayload() {
        String json = """
                {
                  "fantasy_content": {
                    "league": [
                      { "league_key": "123.l.456" },
                      {
                        "teams": {
                          "count": 1,
                          "0": {
                            "team": [
                              [
                                { "team_key": "123.l.456.t.1" },
                                { "name": "My Team" }
                              ]
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

        TeamRepository teamRepository = mock(TeamRepository.class);
        LeagueRepository leagueRepository = mock(LeagueRepository.class);
        LeagueIngestionService leagueIngestionService = mock(LeagueIngestionService.class);

        League league = new League();
        league.setLeagueKey("123.l.456");
        when(leagueIngestionService.parseLeague(any())).thenReturn(league);

        TeamIngestionService service = new TeamIngestionService(
                webClient,
                tokenService,
                new ObjectMapper(),
                teamRepository,
                leagueRepository,
                leagueIngestionService
        );

        service.usurpLeague("123.l.456");

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        Team saved = captor.getValue();

        assertEquals("123.l.456.t.1", saved.getTeamKey());
        assertEquals("My Team", saved.getName());
        assertEquals(league, saved.getLeague());
    }
}
