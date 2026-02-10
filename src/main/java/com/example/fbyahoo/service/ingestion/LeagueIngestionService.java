package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.config.YahooProperties;
import com.example.fbyahoo.model.League;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.service.TokenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

@Service
public class LeagueIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LeagueIngestionService.class);

    private final LeagueRepository leagueRepository;
    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public LeagueIngestionService(
            LeagueRepository leagueRepository,
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper
    ) {
        this.leagueRepository = leagueRepository;
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void ingestAllLeagues() {
        String json = fetchAllLeaguesJson();

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode leaguesNode = root.path("fantasy_content")
                    .path("users").path("0")
                    .path("user").path(1)
                    .path("games").path("0")
                    .path("game").path(1)
                    .path("leagues");

            if (leaguesNode.isMissingNode() || leaguesNode.isNull()) {
                throw new IllegalStateException("Could not find leagues node in response");
            }

            int count = leaguesNode.path("count").asInt(0);
            log.info("Found {} leagues to ingest", count);

            for (int i = 0; i < count; i++) {
                JsonNode leagueWrapper = leaguesNode.path(String.valueOf(i));
                JsonNode leagueNode = leagueWrapper.path("league").path(0);

                if (leagueNode.isMissingNode() || leagueNode.isNull()) {
                    log.warn("Skipping league index {} (missing league node)", i);
                    continue;
                }

                League league = parseLeague(leagueNode);
                leagueRepository.save(league);
                log.info("League {} ingested successfully", league.getLeagueKey());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest leagues", e);
        }

    }

    @Transactional
    public void ingestLeague(String leagueKey) {
        String json = fetchLeagueJson(leagueKey);

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode leagueNode = root.path("fantasy_content")
                    .path("league").path(0);

            if (leagueNode.isMissingNode() || leagueNode.isNull()) {
                throw new IllegalStateException("Could not find league node in response");
            }

            League league = parseLeague(leagueNode);
            leagueRepository.save(league);
            log.info("League {} ingested successfully", leagueKey);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest league " + leagueKey, e);
        }
    }


    private String fetchLeagueJson(String leagueKey) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/league/{leagueKey}")
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

    private String fetchAllLeaguesJson() {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/users;use_login=1/games;game_keys=466/leagues")
                        .queryParam("format", "json")
                        .build()
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

    private League parseLeague(JsonNode leagueNode) {
        String leagueKey = text(leagueNode, "league_key");
        if (leagueKey == null || leagueKey.isBlank()) {
            throw new IllegalStateException("League missing league_key");
        }

        League league = leagueRepository.findById(leagueKey).orElseGet(League::new);

        league.setLeagueKey(leagueKey);
        league.setLeagueId(text(leagueNode, "league_id"));
        league.setName(text(leagueNode, "name"));
        league.setGameCode(text(leagueNode, "game_code"));

        league.setSeason(intOrNull(leagueNode, "season"));

        league.setUrl(text(leagueNode, "url"));
        league.setLogoUrl(text(leagueNode, "logo_url"));

        league.setDraftStatus(text(leagueNode, "draft_status"));
        league.setNumTeams(intOrNull(leagueNode, "num_teams"));

        league.setWeeklyDeadline(text(leagueNode, "weekly_deadline"));
        league.setRosterType(text(leagueNode, "roster_type"));

        league.setLeagueUpdateTimestamp(longOrNull(leagueNode, "league_update_timestamp"));
        league.setScoringType(text(leagueNode, "scoring_type"));
        league.setLeagueType(text(leagueNode, "league_type"));

        league.setRenew(text(leagueNode, "renew"));
        league.setRenewed(text(leagueNode, "renewed"));
        league.setFeloTier(text(leagueNode, "felo_tier"));

        league.setIsHighscore(boolOrNull(leagueNode, "is_highscore"));
        league.setMatchupWeek(intOrNull(leagueNode, "matchup_week"));

        league.setIrisGroupChatId(text(leagueNode, "iris_group_chat_id"));
        league.setShortInvitationUrl(text(leagueNode, "short_invitation_url"));
        league.setAllowAddToDlExtraPos(intOrNull(leagueNode, "allow_add_to_dl_extra_pos"));

        league.setIsProLeague(boolOrNull(leagueNode, "is_pro_league"));
        league.setIsCashLeague(boolOrNull(leagueNode, "is_cash_league"));
        league.setIsPlusLeague(boolOrNull(leagueNode, "is_plus_league"));

        league.setCurrentWeek(intOrNull(leagueNode, "current_week"));
        league.setStartWeek(intOrNull(leagueNode, "start_week"));
        league.setEndWeek(intOrNull(leagueNode, "end_week"));

        league.setCurrentDate(parseDateOrNull(text(leagueNode, "current_date")));
        league.setStartDate(parseDateOrNull(text(leagueNode, "start_date")));
        league.setEndDate(parseDateOrNull(text(leagueNode, "end_date")));

        if (league.getIngestedAt() == null) {
            league.setIngestedAt(Instant.now());
        }
        league.setUpdatedAt(Instant.now());

        return league;




    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static Integer intOrNull(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private static Long longOrNull(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private static Boolean boolOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;

        // Yahoo sometimes sends boolean true/false, sometimes "0"/"1"
        if (v.isBoolean()) return v.asBoolean();
        String s = v.asText();
        if ("1".equals(s) || "true".equalsIgnoreCase(s)) return true;
        if ("0".equals(s) || "false".equalsIgnoreCase(s)) return false;
        return null;
    }

    private static java.time.LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank() || s.equals("-")) return null;
        return java.time.LocalDate.parse(s);
    }

}
