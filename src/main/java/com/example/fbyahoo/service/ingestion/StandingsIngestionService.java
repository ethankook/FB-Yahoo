package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.model.Team;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.TeamRepository;
import com.example.fbyahoo.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class StandingsIngestionService {

    private static final Logger log = LoggerFactory.getLogger(StandingsIngestionService.class);

    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    public StandingsIngestionService(
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper,
            LeagueRepository leagueRepository,
            TeamRepository teamRepository
    ) {
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public void ingestAllStandings() {
        for (League league : leagueRepository.findAll()) {
            ingestStandings(league.getLeagueKey());
        }
    }

    @Transactional
    public void ingestStandings(String leagueKey) {
        String json = fetchStandingsJson(leagueKey);

        try {
            JsonNode root = objectMapper.readTree(json);

            JsonNode teamsNode = root.path("fantasy_content")
                    .path("league").path(1)
                    .path("standings").path(0)
                    .path("teams");

            if (teamsNode.isMissingNode() || teamsNode.isNull()) {
                log.warn("No standings data found for league {}", leagueKey);
                return;
            }

            int count = teamsNode.path("count").asInt(0);
            log.info("Ingesting standings for {} teams in league {}", count, leagueKey);

            for (int i = 0; i < count; i++) {
                JsonNode teamWrapper = teamsNode.path(String.valueOf(i));
                JsonNode teamOuter = teamWrapper.path("team");

                JsonNode teamFields = teamOuter.path(0);
                if (teamFields.isMissingNode() || !teamFields.isArray()) continue;

                String teamKey = null;
                for (JsonNode item : teamFields) {
                    if (item != null && item.isObject() && item.has("team_key")) {
                        teamKey = text(item, "team_key");
                        break;
                    }
                }
                if (teamKey == null) continue;

                JsonNode standingsData = teamOuter.path(2);
                if (standingsData.isMissingNode()) {
                    standingsData = teamOuter.path(1);
                }

                JsonNode teamStandings = standingsData.path("team_standings");
                if (teamStandings.isMissingNode()) continue;

                Team team = teamRepository.findById(teamKey).orElse(null);
                if (team == null) {
                    log.warn("Team {} not found in DB, skipping standings", teamKey);
                    continue;
                }

                team.setStandingRank(intOrNull(teamStandings, "rank"));

                JsonNode outcome = teamStandings.path("outcome_totals");
                team.setStandingWins(intOrNull(outcome, "wins"));
                team.setStandingLosses(intOrNull(outcome, "losses"));
                team.setStandingTies(intOrNull(outcome, "ties"));
                team.setStandingPct(bigDecimalOrNull(text(outcome, "percentage")));

                team.setStandingGamesBack(bigDecimalOrNull(text(teamStandings, "games_back")));
                team.setStandingPointsFor(bigDecimalOrNull(text(teamStandings, "points_for")));
                team.setStandingPointsAgainst(bigDecimalOrNull(text(teamStandings, "points_against")));

                team.setUpdatedAt(Instant.now());
                teamRepository.save(team);
                log.info("Standings updated for team {} (rank={})", teamKey, team.getStandingRank());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest standings for league " + leagueKey, e);
        }
    }

    private String fetchStandingsJson(String leagueKey) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/league/{leagueKey}/standings")
                        .queryParam("format", "json")
                        .build(leagueKey)
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Yahoo API error: " + resp.statusCode() + " body=" + body)))
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .block();
    }
}
