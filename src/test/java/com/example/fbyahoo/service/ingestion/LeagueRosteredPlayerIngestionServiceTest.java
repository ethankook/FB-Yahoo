package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.LeagueRosteredPlayerRepository;
import com.example.fbyahoo.repo.PlayerRepository;
import com.example.fbyahoo.repo.TeamRepository;
import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.testutil.WebClientTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.mockito.Mockito.*;

class LeagueRosteredPlayerIngestionServiceTest {

    @Test
    void syncLeagueRosteredPlayers_upsertsAndDeletesStale() {
        String json = """
                {
                  "fantasy_content": {
                    "league": [
                      {},
                      {
                        "players": {
                          "0": {
                            "player": [
                              [ { "player_id": "p1" }, { "player_key": "game.p1" } ],
                              { "ownership": { "owner_team_key": "t1" } }
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

        LeagueRepository leagueRepository = mock(LeagueRepository.class);
        TeamRepository teamRepository = mock(TeamRepository.class);
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        LeagueRosteredPlayerRepository lrpRepository = mock(LeagueRosteredPlayerRepository.class);

        League league = new League();
        league.setLeagueKey("l1");
        when(leagueRepository.findById("l1")).thenReturn(Optional.of(league));

        Team team = new Team();
        team.setTeamKey("t1");
        when(teamRepository.findById("t1")).thenReturn(Optional.of(team));

        when(playerRepository.findById("p1")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lrpRepository.deleteByLeague_LeagueKeyAndAsOfBefore(eq("l1"), any())).thenReturn(0L);

        LeagueRosteredPlayerIngestionService service = new LeagueRosteredPlayerIngestionService(
                leagueRepository,
                teamRepository,
                playerRepository,
                lrpRepository,
                webClient,
                tokenService,
                new ObjectMapper()
        );

        service.syncLeagueRosteredPlayers("l1");

        verify(lrpRepository).save(any());
        verify(lrpRepository).deleteByLeague_LeagueKeyAndAsOfBefore(eq("l1"), any());
    }
}
