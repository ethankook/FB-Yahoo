package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerOwnership;
import com.example.fbyahoo.model.PlayerOwnershipId;
import com.example.fbyahoo.repo.PlayerOwnershipRepository;
import com.example.fbyahoo.repo.PlayerRepository;
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
import java.util.Optional;

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class PlayerOwnershipIngestionService {

    private static final Logger log = LoggerFactory.getLogger(PlayerOwnershipIngestionService.class);

    private static final String GAME_KEY = "466";
    private static final int SEASON = 2025;

    private final PlayerOwnershipRepository ownershipRepository;
    private final PlayerRepository playerRepository;
    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public PlayerOwnershipIngestionService(
            PlayerOwnershipRepository ownershipRepository,
            PlayerRepository playerRepository,
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper
    ) {
        this.ownershipRepository = ownershipRepository;
        this.playerRepository = playerRepository;
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void ingestAllPlayerOwnership() {
        final int pageSize = 25;
        int start = 0;
        boolean hasMore = true;
        int totalIngested = 0;

        while (hasMore) {
            String json = fetchPlayersJson(start, pageSize);

            try {
                JsonNode root = objectMapper.readTree(json);

                JsonNode playersNode = root.path("fantasy_content")
                        .path("game").path(1)
                        .path("players");

                if (playersNode.isMissingNode() || playersNode.isNull()) {
                    throw new IllegalStateException("Could not find players node at fantasy_content.game[1].players");
                }

                int foundThisPage = 0;

                for (int i = 0; i < pageSize; i++) {
                    JsonNode wrapper = playersNode.path(String.valueOf(i));
                    if (wrapper.isMissingNode() || wrapper.isNull()) break;

                    JsonNode playerNode = wrapper.path("player");
                    if (playerNode.isMissingNode() || playerNode.isNull()) continue;

                    foundThisPage++;
                    if (upsertOwnership(playerNode)) {
                        totalIngested++;
                    }
                }

                if (foundThisPage == 0) {
                    log.warn("No players found on page start={}, stopping", start);
                    break;
                }

                log.info("Processed {} players for ownership (start={})", foundThisPage, start);
                start += pageSize;
                hasMore = foundThisPage == pageSize;

            } catch (Exception e) {
                throw new RuntimeException("Failed to ingest player ownership (start=" + start + ")", e);
            }
        }

        log.info("Finished ingesting player ownership. total={}", totalIngested);
    }

    private String fetchPlayersJson(int start, int count) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/game/{gameKey}/players;start={start};count={count};out=percent_owned")
                        .queryParam("format", "json")
                        .build(GAME_KEY, start, count)
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Yahoo API error: " + resp.statusCode() + " body=" + body)))
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(15))
                .block();
    }

    private boolean upsertOwnership(JsonNode playerNode) {
        JsonNode playerArray = playerNode.path(0);
        String playerId = extractPlayerId(playerArray);

        if (playerId == null || playerId.isBlank()) {
            log.warn("Skipping player with missing player_id");
            return false;
        }

        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.warn("Skipping playerId={} (not found in player table)", playerId);
            return false;
        }

        JsonNode ownershipWrapper = playerNode.path(1);
        if (ownershipWrapper.isMissingNode() || ownershipWrapper.isNull()
                || !ownershipWrapper.has("percent_owned")) {
            log.warn("Skipping playerId={} (no percent_owned data)", playerId);
            return false;
        }

        JsonNode percentOwnedNode = ownershipWrapper.path("percent_owned");

        Integer week = null;
        Integer value = null;
        Integer delta = null;

        // percent_owned is an array of singleton objects: [{coverage_type, week}, {value}, {delta}]
        if (percentOwnedNode.isArray()) {
            for (JsonNode entry : percentOwnedNode) {
                if (entry == null || !entry.isObject()) continue;
                if (entry.has("week")) week = intOrNull(entry, "week");
                if (entry.has("value")) value = intOrNull(entry, "value");
                if (entry.has("delta")) delta = intOrNull(entry, "delta");
            }
        } else if (percentOwnedNode.isObject()) {
            week = intOrNull(percentOwnedNode, "week");
            value = intOrNull(percentOwnedNode, "value");
            delta = intOrNull(percentOwnedNode, "delta");
        }

        if (value == null) {
            log.warn("Skipping playerId={} (null percent_owned value)", playerId);
            return false;
        }

        if (week == null) {
            week = 0;
        }

        PlayerOwnershipId id = new PlayerOwnershipId(playerId, SEASON);
        PlayerOwnership ownership = ownershipRepository.findById(id)
                .orElseGet(PlayerOwnership::new);

        ownership.setId(id);
        ownership.setPlayer(playerOpt.get());
        ownership.setWeek(week);
        ownership.setPercentOwned(value);
        ownership.setDeltaWeek(delta);
        ownership.setUpdatedAt(Instant.now());

        ownershipRepository.save(ownership);
        return true;
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
}
