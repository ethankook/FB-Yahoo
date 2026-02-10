package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.repo.PlayerRepository;
import com.example.fbyahoo.repo.PlayerStatsRepository;
import com.example.fbyahoo.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.UnknownHttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class PlayerStatsIngestionService {

    private static final Logger log = LoggerFactory.getLogger(PlayerStatsIngestionService.class);

    private static final String GAME_KEY = "466";

    private final PlayerStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public PlayerStatsIngestionService(
            PlayerStatsRepository statsRepository,
            PlayerRepository playerRepository,
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper
    ) {
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void ingestSeasonAveragesForAllPlayers() {
        log.info("Starting player stats ingestion (season averages)");

        String accessToken = tokenService.getValidAccessToken();

        int pageSize = 10;
        int start = 0;
        int totalUpserts = 0;

        while (true) {
            String json = fetchGamePlayersWithStatsJson(accessToken, start, pageSize);

            JsonNode root;
            try {
                root = objectMapper.readTree(json);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Yahoo response start=" + start, e);
            }

            JsonNode playersNode = root.path("fantasy_content")
                    .path("game").path(1)
                    .path("players");

            if (playersNode.isMissingNode() || playersNode.isNull()) {
                break;
            }

            int foundThisPage = 0;

            for (int i = 0; i < pageSize; i++) {
                JsonNode wrapper = playersNode.path(String.valueOf(i));
                if (wrapper.isMissingNode() || wrapper.isNull()) break;

                JsonNode playerNode = wrapper.path("player");
                if (playerNode.isMissingNode() || playerNode.isNull()) continue;

                foundThisPage++;

                try {
                    if (upsertSeasonStatsFromGamePlayersPayload(playerNode)) {
                        totalUpserts++;
                    }
                } catch (Exception e) {
                    String playerId = extractPlayerId(playerNode.path(0));
                    log.warn("Failed player stats ingest for playerId={}", safe(playerId), e);
                }
            }

            if (foundThisPage < pageSize) {
                break;
            }

            start += pageSize;
        }

        log.info("Finished player stats ingestion. total={}", totalUpserts);
    }

    private boolean upsertSeasonStatsFromGamePlayersPayload(JsonNode playerNode) {
        JsonNode playerArray = playerNode.path(0);
        String playerId = extractPlayerId(playerArray);
        if (playerId == null || playerId.isBlank()) return false;

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return false;

        JsonNode statsWrapper = playerNode.path(1);
        if (statsWrapper.isMissingNode() || statsWrapper.isNull() || !statsWrapper.has("player_stats")) {
            return false;
        }

        JsonNode playerStatsNode = statsWrapper.path("player_stats");
        JsonNode statsArray = playerStatsNode.path("stats");

        Map<String, String> statMap = new HashMap<>();
        if (statsArray.isArray()) {
            for (JsonNode statWrapper : statsArray) {
                JsonNode stat = statWrapper.path("stat");
                if (stat.isMissingNode() || stat.isNull()) continue;
                String statId = text(stat, "stat_id");
                String value = text(stat, "value");
                if (statId != null) statMap.put(statId, value);
            }
        }

        Integer gamesPlayed = parseIntStat(statMap.get("0"));
        if (gamesPlayed == null || gamesPlayed == 0) return false;

        BigDecimal gp = new BigDecimal(gamesPlayed);

        PlayerStats stats = statsRepository.findById(playerId).orElseGet(PlayerStats::new);

        stats.setId(playerId);
        stats.setGamesPlayed(gamesPlayed);
        stats.setSeason(Integer.valueOf(statMap.get("2")));
        stats.setFgAttPg(perGame(statMap.get("3"), gp));
        stats.setFgMadePg(perGame(statMap.get("4"), gp));
        stats.setFtAttPg(perGame(statMap.get("6"), gp));
        stats.setFtMadePg(perGame(statMap.get("7"), gp));
        stats.setFg3AttPg(perGame(statMap.get("9"), gp));
        stats.setFg3MadePg(perGame(statMap.get("10"), gp));

        stats.setPtsPg(perGame(statMap.get("12"), gp));
        stats.setRebPg(perGame(statMap.get("15"), gp));
        stats.setAstPg(perGame(statMap.get("16"), gp));
        stats.setStlPg(perGame(statMap.get("17"), gp));
        stats.setBlkPg(perGame(statMap.get("18"), gp));
        stats.setTovPg(perGame(statMap.get("19"), gp));

        stats.setFgPct(bigDecimalOrNull(statMap.get("5")));
        stats.setFtPct(bigDecimalOrNull(statMap.get("8")));
        stats.setFg3Pct(bigDecimalOrNull(statMap.get("11")));

        stats.setUpdatedAt(Instant.now());

        statsRepository.save(stats);
        return true;
    }

    private String fetchGamePlayersWithStatsJson(String accessToken, int start, int count) {
        String path = "/fantasy/v2/game/{gameKey}/players;start={start};count={count};out=stats";
        return getWithRetry(accessToken, path, new Object[]{GAME_KEY, start, count});
    }

    private String getWithRetry(String accessToken, String path, Object[] uriArgs) {
        int attempts = 3;
        long backoffMs = 500;

        for (int i = 1; i <= attempts; i++) {
            try {
                return fantasyClient.get()
                        .uri(uriBuilder -> uriBuilder.path(path).queryParam("format", "json").build(uriArgs))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(20))
                        .block();
            } catch (UnknownHttpStatusCodeException e) {
                int code = e.getStatusCode().value();
                if (i < attempts && (code == 999 || code == 429 || (code >= 500 && code <= 599))) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 6000);
                    continue;
                }
                throw e;
            } catch (Exception e) {
                if (i < attempts) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 6000);
                    continue;
                }
                throw e;
            }
        }

        throw new IllegalStateException("unreachable");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private BigDecimal perGame(String totalStr, BigDecimal gp) {
        BigDecimal total = bigDecimalOrNull(totalStr);
        if (total == null) return null;
        return total.divide(gp, 3, RoundingMode.HALF_UP);
    }

    private Integer parseIntStat(String s) {
        if (s == null || s.isBlank() || s.equals("-")) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private String extractPlayerId(JsonNode playerArray) {
        if (playerArray == null || !playerArray.isArray()) return null;
        for (JsonNode entry : playerArray) {
            if (entry != null && entry.isObject() && entry.has("player_id")) {
                return text(entry, "player_id");
            }
        }
        return null;
    }

    private String safe(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}