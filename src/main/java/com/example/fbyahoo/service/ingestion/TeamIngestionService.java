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
public class TeamIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TeamIngestionService.class);


    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueIngestionService leagueIngestionService;

    public TeamIngestionService(
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper,
            TeamRepository teamRepository,
            LeagueRepository leagueRepository,
            LeagueIngestionService leagueIngestionService
    )
    {
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.teamRepository = teamRepository;
        this.leagueRepository = leagueRepository;
        this.leagueIngestionService = leagueIngestionService;
    }

    @Transactional
    public void ingestAllTeams() {
        for (League league : leagueRepository.findAll()) {
            usurpLeague(league.getLeagueKey());
        }
    }


    @Transactional
    public void usurpLeague(String leagueKey) {
        String json = fetchLeagueJson(leagueKey);

        try {
            JsonNode root = objectMapper.readTree(json);

            JsonNode leagueNode = root.path("fantasy_content").path("league").path(0);

            if (leagueNode.isMissingNode() || leagueNode.isNull()) {
                throw new IllegalStateException("Could not find league node at fantasy_content.league[0]");
            }

            League league = leagueIngestionService.parseLeague(leagueNode);
            leagueRepository.save(league);

            JsonNode teamsNode = root.path("fantasy_content")
                    .path("league").path(1)
                    .path("teams");

            if (teamsNode.isMissingNode() || teamsNode.isNull()) {
                throw new IllegalStateException("Could not find teams node at fantasy_content.league[1].teams");
            }

            int count = teamsNode.path("count").asInt(0);
            log.info("Found {} teams to ingest for league {}", count, leagueKey);

            for (int i = 0; i < count; i++) {
                JsonNode teamWrapper = teamsNode.path(String.valueOf(i));
                JsonNode teamOuter = teamWrapper.path("team");

                JsonNode teamFields = teamOuter.path(0);
                if (teamFields.isMissingNode() || teamFields.isNull() || !teamFields.isArray()) {
                    log.warn("Skipping team index {} (missing team fields array)", i);
                    continue;
                }

                Team team = parseTeam(teamFields, league);
                teamRepository.save(team);
                log.info("Team {} ingested successfully", team.getTeamKey());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to usurp league " + leagueKey, e);
        }
    }

    private String fetchLeagueJson(String leagueKey) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/league/{leagueKey}/teams")
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

    private Team parseTeam(JsonNode teamFields, League league) {
        String teamKey = null;
        for (JsonNode item : teamFields) {
            if (item == null || item.isNull() || item.isMissingNode()) continue;
            if (item.isObject() && item.has("team_key")) {
                teamKey = text(item, "team_key");
                break;
            }
        }

        if (teamKey == null || teamKey.isBlank()) {
            throw new IllegalStateException("Team missing team_key");
        }

        Team team = teamRepository.findById(teamKey).orElseGet(Team::new);

        team.setTeamKey(teamKey);
        team.setLeague(league);

        for (JsonNode item : teamFields) {
            if (item == null || item.isNull() || item.isMissingNode()) continue;
            if (item.isArray()) continue;

            if (!item.isObject()) continue;

            if (item.has("team_id")) {
                team.setTeamId(text(item, "team_id"));
            } else if (item.has("name")) {
                team.setName(text(item, "name"));
            } else if (item.has("is_owned_by_current_login")) {
                team.setIsOwnedByCurrentLogin(boolOrNull(item, "is_owned_by_current_login"));
            } else if (item.has("url")) {
                team.setUrl(text(item, "url"));
            } else if (item.has("team_logos")) {
                team.setLogoUrl(extractLogoUrl(item.path("team_logos")));
            } else if (item.has("waiver_priority")) {
                team.setWaiverPriority(intOrNull(item, "waiver_priority"));
            } else if (item.has("number_of_moves")) {
                team.setNumberOfMoves(intOrNull(item, "number_of_moves"));
            } else if (item.has("number_of_trades")) {
                team.setNumberOfTrades(intOrNull(item, "number_of_trades"));
            } else if (item.has("roster_adds")) {
                JsonNode ra = item.path("roster_adds");
                team.setRosterAddsCoverageType(text(ra, "coverage_type"));
                team.setRosterAddsCoverageValue(intOrNull(ra, "coverage_value"));
                team.setRosterAddsValue(intOrNull(ra, "value"));
            } else if (item.has("clinched_playoffs")) {
                team.setClinchedPlayoffs(boolOrNull(item, "clinched_playoffs"));
            } else if (item.has("league_scoring_type")) {
                team.setLeagueScoringType(text(item, "league_scoring_type"));
            } else if (item.has("draft_position")) {
                team.setDraftPosition(intOrNull(item, "draft_position"));
            } else if (item.has("has_draft_grade")) {
                team.setHasDraftGrade(boolOrNull(item, "has_draft_grade"));
            } else if (item.has("managers")) {
                JsonNode manager = firstManagerNode(item.path("managers"));
                if (manager != null) {
                    team.setManagerId(text(manager, "manager_id"));
                    team.setManagerNickname(text(manager, "nickname"));
                    team.setManagerGuid(text(manager, "guid"));
                    team.setManagerIsCommissioner(boolOrNull(manager, "is_commissioner"));
                    team.setManagerIsCurrentLogin(boolOrNull(manager, "is_current_login"));
                    team.setManagerEmail(text(manager, "email"));
                    team.setManagerImageUrl(text(manager, "image_url"));
                    team.setManagerFeloScore(intOrNull(manager, "felo_score"));
                    team.setManagerFeloTier(text(manager, "felo_tier"));
                }
            }
        }

        if (team.getIngestedAt() == null) {
            team.setIngestedAt(Instant.now());
        }
        team.setUpdatedAt(Instant.now());

        return team;
    }

    private static String extractLogoUrl(JsonNode teamLogos) {
        if (teamLogos == null || teamLogos.isNull() || teamLogos.isMissingNode()) return null;

        if (teamLogos.isArray()) {
            for (JsonNode entry : teamLogos) {
                JsonNode logo = entry.path("team_logo");
                String url = text(logo, "url");
                if (url != null && !url.isBlank()) return url;
            }
        }
        return null;
    }

    private static JsonNode firstManagerNode(JsonNode managers) {
        if (managers == null || managers.isNull() || managers.isMissingNode()) return null;
        if (!managers.isArray() || managers.size() == 0) return null;

        JsonNode manager = managers.get(0).path("manager");
        if (manager.isMissingNode() || manager.isNull()) return null;
        return manager;
    }
}
