package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.Player;
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
import java.util.ArrayList;
import java.util.List;

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class PlayerIngestionService {

    private static final Logger log = LoggerFactory.getLogger(PlayerIngestionService.class);

    private final PlayerRepository playerRepository;
    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public PlayerIngestionService(
            PlayerRepository playerRepository,
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper
    ) {
        this.playerRepository = playerRepository;
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    /**
     * Usurp (upsert) ALL players for the game by paging through /game/{gameKey}/players.
     */
    @Transactional
    public void usurpAllPlayers() {
        final String gameKey = "466";
        final int pageSize = 25;

        int start = 0;
        boolean hasMore = true;

        while (hasMore) {
            String json = fetchAllPlayersJson(gameKey, start, pageSize);

            try {
                JsonNode root = objectMapper.readTree(json);

                JsonNode playersNode = root.path("fantasy_content")
                        .path("game").path(1)
                        .path("players");

                if (playersNode.isMissingNode() || playersNode.isNull()) {
                    throw new IllegalStateException("Could not find players node at fantasy_content.game[1].players");
                }

                int ingestedThisPage = 0;

                for (int i = 0; i < pageSize; i++) {
                    JsonNode wrapper = playersNode.path(String.valueOf(i));
                    if (wrapper.isMissingNode() || wrapper.isNull()) break;

                    JsonNode playerArray = wrapper.path("player").path(0);
                    if (playerArray.isMissingNode() || playerArray.isNull()) continue;

                    Player player = parsePlayer(playerArray);
                    playerRepository.save(player);
                    ingestedThisPage++;
                }

                if (ingestedThisPage == 0) {
                    log.warn("No players found on page start={}, stopping to avoid infinite loop", start);
                    break;
                }

                log.info("Ingested {} players (start={})", ingestedThisPage, start);
                start += pageSize;

                hasMore = ingestedThisPage == pageSize;

            } catch (Exception e) {
                throw new RuntimeException("Failed to usurp all players (start=" + start + ")", e);
            }
        }

        log.info("Finished usurping all players.");
    }

    private String fetchAllPlayersJson(String gameKey, int start, int count) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/game/{gameKey}/players;start={start};count={count}")
                        .queryParam("format", "json")
                        .build(gameKey, start, count)
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

    /**
     * Parse a Yahoo "player" array (the inner player[0] array of singleton objects).
     * This matches your provided JSON exactly.
     */
    public Player parsePlayer(JsonNode playerArray) {
        if (playerArray == null || !playerArray.isArray()) {
            throw new IllegalArgumentException("Expected player array node");
        }

        String playerKey = null;
        String playerId = null;

        JsonNode nameNode = null;
        JsonNode headshotNode = null;
        JsonNode eligiblePositionsNode = null;

        String url = null;
        String status = null;
        String statusFull = null;
        String injuryNote = null;

        String editorialPlayerKey = null;
        String editorialTeamKey = null;
        String editorialTeamFullName = null;
        String editorialTeamAbbr = null;
        String editorialTeamUrl = null;

        String uniformNumber = null;
        String displayPosition = null;

        String imageUrl = null;
        Boolean isUndroppable = null;
        String positionType = null;

        // Walk the array and pick out singleton objects by key
        for (JsonNode entry : playerArray) {
            if (entry == null || !entry.isObject()) continue;

            if (entry.has("player_key")) playerKey = text(entry, "player_key");
            else if (entry.has("player_id")) playerId = text(entry, "player_id");
            else if (entry.has("name")) nameNode = entry.path("name");
            else if (entry.has("url")) url = text(entry, "url");
            else if (entry.has("status")) status = text(entry, "status");
            else if (entry.has("status_full")) statusFull = text(entry, "status_full");
            else if (entry.has("injury_note")) injuryNote = text(entry, "injury_note");
            else if (entry.has("editorial_player_key")) editorialPlayerKey = text(entry, "editorial_player_key");
            else if (entry.has("editorial_team_key")) editorialTeamKey = text(entry, "editorial_team_key");
            else if (entry.has("editorial_team_full_name")) editorialTeamFullName = text(entry, "editorial_team_full_name");
            else if (entry.has("editorial_team_abbr")) editorialTeamAbbr = text(entry, "editorial_team_abbr");
            else if (entry.has("editorial_team_url")) editorialTeamUrl = text(entry, "editorial_team_url");
            else if (entry.has("uniform_number")) uniformNumber = text(entry, "uniform_number");
            else if (entry.has("display_position")) displayPosition = text(entry, "display_position");
            else if (entry.has("headshot")) headshotNode = entry.path("headshot");
            else if (entry.has("image_url")) imageUrl = text(entry, "image_url");
            else if (entry.has("is_undroppable")) isUndroppable = boolOrNull(entry, "is_undroppable");
            else if (entry.has("position_type")) positionType = text(entry, "position_type");
            else if (entry.has("eligible_positions")) eligiblePositionsNode = entry.path("eligible_positions");
        }

        if (playerId == null || playerId.isBlank()) {
            throw new IllegalStateException("Player missing player_id");
        }
        if (playerKey == null || playerKey.isBlank()) {
            throw new IllegalStateException("Player missing player_key");
        }

        Player player = playerRepository.findById(playerId).orElseGet(Player::new);

        player.setPlayerId(playerId);
        player.setPlayerKey(playerKey);

        if (nameNode != null && !nameNode.isMissingNode() && !nameNode.isNull()) {
            player.setNameFull(text(nameNode, "full"));
            player.setNameFirst(text(nameNode, "first"));
            player.setNameLast(text(nameNode, "last"));
            player.setNameAsciiFirst(text(nameNode, "ascii_first"));
            player.setNameAsciiLast(text(nameNode, "ascii_last"));
        }

        player.setUrl(url);
        player.setStatus(status);
        player.setStatusFull(statusFull);
        player.setInjuryNote(injuryNote);

        player.setEditorialPlayerKey(editorialPlayerKey);
        player.setEditorialTeamKey(editorialTeamKey);
        player.setEditorialTeamFullName(editorialTeamFullName);
        player.setEditorialTeamAbbr(editorialTeamAbbr);
        player.setEditorialTeamUrl(editorialTeamUrl);

        player.setUniformNumber(uniformNumber);
        player.setDisplayPosition(displayPosition);

        if (headshotNode != null && !headshotNode.isMissingNode() && !headshotNode.isNull()) {
            player.setHeadshotUrl(text(headshotNode, "url"));
            player.setHeadshotSize(text(headshotNode, "size"));
        }

        player.setImageUrl(imageUrl);
        player.setIsUndroppable(isUndroppable);
        player.setPositionType(positionType);

        player.setEligiblePositions(parseEligiblePositions(eligiblePositionsNode));

        if (player.getIngestedAt() == null) {
            player.setIngestedAt(Instant.now());
        }
        player.setUpdatedAt(Instant.now());

        return player;
    }

    private String[] parseEligiblePositions(JsonNode eligiblePositionsNode) {
        if (eligiblePositionsNode == null || eligiblePositionsNode.isMissingNode() || eligiblePositionsNode.isNull()) {
            return new String[0];
        }

        List<String> positions = new ArrayList<>();
        for (JsonNode entry : eligiblePositionsNode) {
            String pos = text(entry, "position");
            if (pos != null && !pos.isBlank()) {
                positions.add(pos);
            }
        }
        return positions.toArray(new String[0]);
    }
}