package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.League;
import com.example.fbyahoo.model.Matchup;
import com.example.fbyahoo.model.MatchupStat;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.MatchupRepository;
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

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class MatchupIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MatchupIngestionService.class);

    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final LeagueRepository leagueRepository;
    private final MatchupRepository matchupRepository;

    public MatchupIngestionService(
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper,
            LeagueRepository leagueRepository,
            MatchupRepository matchupRepository
    ) {
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.leagueRepository = leagueRepository;
        this.matchupRepository = matchupRepository;
    }

    @Transactional
    public void ingestAllMatchups() {
        for (League league : leagueRepository.findAll()) {
            Integer week = league.getCurrentWeek();
            if (week == null) week = 1;
            ingestMatchups(league.getLeagueKey(), week);
        }
    }

    @Transactional
    public void ingestMatchups(String leagueKey, int week) {
        String json = fetchScoreboardJson(leagueKey, week);

        try {
            JsonNode root = objectMapper.readTree(json);

            JsonNode scoreboardNode = root.path("fantasy_content")
                    .path("league").path(1)
                    .path("scoreboard").path(0)
                    .path("matchups");

            if (scoreboardNode.isMissingNode() || scoreboardNode.isNull()) {
                log.warn("No scoreboard data found for league {} week {}", leagueKey, week);
                return;
            }

            int count = scoreboardNode.path("count").asInt(0);
            log.info("Ingesting {} matchups for league {} week {}", count, leagueKey, week);

            for (int i = 0; i < count; i++) {
                JsonNode matchupNode = scoreboardNode.path(String.valueOf(i)).path("matchup");
                if (matchupNode.isMissingNode()) continue;

                parseAndSaveMatchup(leagueKey, week, matchupNode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest matchups for league " + leagueKey + " week " + week, e);
        }
    }

    private void parseAndSaveMatchup(String leagueKey, int week, JsonNode matchupNode) {
        JsonNode teamsNode = matchupNode.path("0").path("teams");
        if (teamsNode.isMissingNode()) return;

        String team1Key = extractTeamKey(teamsNode.path("0").path("team").path(0));
        String team2Key = extractTeamKey(teamsNode.path("1").path("team").path(0));

        if (team1Key == null || team2Key == null) {
            log.warn("Could not extract team keys from matchup, skipping");
            return;
        }

        Boolean isTied = boolOrNull(matchupNode, "is_tied");
        String winnerTeamKey = text(matchupNode, "winner_team_key");

        // Upsert matchup
        Matchup matchup = matchupRepository.findByLeagueKeyAndWeekAndTeam1Key(leagueKey, week, team1Key)
                .orElseGet(Matchup::new);

        matchup.setLeagueKey(leagueKey);
        matchup.setWeek(week);
        matchup.setTeam1Key(team1Key);
        matchup.setTeam2Key(team2Key);
        matchup.setIsTied(isTied);
        matchup.setWinnerTeamKey(winnerTeamKey);

        // Clear old stats and re-parse
        matchup.getStats().clear();

        // Parse stat_winners
        JsonNode statWinners = matchupNode.path("stat_winners");
        if (statWinners.isArray()) {
            for (JsonNode sw : statWinners) {
                JsonNode statWinner = sw.path("stat_winner");
                if (statWinner.isMissingNode()) continue;

                Integer statId = intOrNull(statWinner, "stat_id");
                if (statId == null) continue;

                String statName = mapStatIdToName(statId);
                Boolean statTied = boolOrNull(statWinner, "is_tied");
                String statWinnerKey = text(statWinner, "winner_team_key");

                // Get stat values from teams
                var team1Value = getTeamStatValue(teamsNode.path("0").path("team"), statId);
                var team2Value = getTeamStatValue(teamsNode.path("1").path("team"), statId);

                MatchupStat ms = MatchupStat.builder()
                        .matchup(matchup)
                        .statId(statId)
                        .statName(statName)
                        .team1Value(team1Value)
                        .team2Value(team2Value)
                        .isTied(statTied)
                        .winnerTeamKey(statWinnerKey)
                        .build();

                matchup.getStats().add(ms);
            }
        }

        matchupRepository.save(matchup);
        log.info("Matchup saved: {} vs {} week {} ({} stats)", team1Key, team2Key, week, matchup.getStats().size());
    }

    private String extractTeamKey(JsonNode teamFields) {
        if (teamFields == null || !teamFields.isArray()) return null;
        for (JsonNode item : teamFields) {
            if (item != null && item.isObject() && item.has("team_key")) {
                return text(item, "team_key");
            }
        }
        return null;
    }

    private java.math.BigDecimal getTeamStatValue(JsonNode teamNode, int statId) {
        // team[1].team_stats.stats is array of {stat: {stat_id, value}}
        JsonNode statsNode = teamNode.path(1).path("team_stats").path("stats");
        if (statsNode.isMissingNode() || !statsNode.isArray()) return null;

        for (JsonNode statEntry : statsNode) {
            JsonNode stat = statEntry.path("stat");
            Integer id = intOrNull(stat, "stat_id");
            if (id != null && id == statId) {
                return bigDecimalOrNull(text(stat, "value"));
            }
        }
        return null;
    }

    private String mapStatIdToName(int statId) {
        return switch (statId) {
            case 5 -> "FG%";
            case 8 -> "FT%";
            case 10 -> "3PTM";
            case 12 -> "PTS";
            case 15 -> "REB";
            case 16 -> "AST";
            case 17 -> "STL";
            case 18 -> "BLK";
            case 19 -> "TO";
            default -> "STAT_" + statId;
        };
    }

    private String fetchScoreboardJson(String leagueKey, int week) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/league/{leagueKey}/scoreboard")
                        .queryParam("week", week)
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
