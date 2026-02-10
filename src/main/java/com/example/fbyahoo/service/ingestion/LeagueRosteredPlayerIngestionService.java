package com.example.fbyahoo.service.ingestion;

import com.example.fbyahoo.model.*;
import com.example.fbyahoo.repo.LeagueRepository;
import com.example.fbyahoo.repo.LeagueRosteredPlayerRepository;
import com.example.fbyahoo.repo.PlayerRepository;
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
import java.util.Optional;

import static com.example.fbyahoo.util.YahooJson.*;

@Service
public class LeagueRosteredPlayerIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LeagueRosteredPlayerIngestionService.class);

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueRosteredPlayerRepository lrpRepository;

    private final WebClient fantasyClient;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public LeagueRosteredPlayerIngestionService(
            LeagueRepository leagueRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            LeagueRosteredPlayerRepository lrpRepository,
            @Qualifier("yahooFantasyClient") WebClient fantasyClient,
            TokenService tokenService,
            ObjectMapper objectMapper
    ) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.lrpRepository = lrpRepository;
        this.fantasyClient = fantasyClient;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void syncAllLeaguesRosteredPlayers() {
        var leagues = leagueRepository.findAll();
        log.info("Syncing league_rostered_player for {} leagues", leagues.size());

        for (League league : leagues) {
            syncLeagueRosteredPlayers(league.getLeagueKey());
        }
    }

    @Transactional
    public void syncLeagueRosteredPlayers(String leagueKey) {
        League league = leagueRepository.findById(leagueKey)
                .orElseThrow(() -> new IllegalStateException("League not found in DB: " + leagueKey));

        final int pageSize = 10;
        int start = 0;
        boolean hasMore = true;

        Instant runAsOf = Instant.now();

        int totalUpserted = 0;
        int totalSkippedMissingTeam = 0;

        while (hasMore) {
            String json = fetchLeagueRosteredPlayersJson(leagueKey, start, pageSize);

            try {
                JsonNode root = objectMapper.readTree(json);

                JsonNode playersNode = root.path("fantasy_content")
                        .path("league").path(1)
                        .path("players");

                if (playersNode.isMissingNode() || playersNode.isNull()) {
                    throw new IllegalStateException("Could not find players node at fantasy_content.league[1].players");
                }

                int foundThisPage = 0;
                int upsertedThisPage = 0;

                for (int i = 0; i < pageSize; i++) {
                    JsonNode wrapper = playersNode.path(String.valueOf(i));
                    if (wrapper.isMissingNode() || wrapper.isNull()) break;

                    JsonNode playerNode = wrapper.path("player");
                    if (playerNode.isMissingNode() || playerNode.isNull()) continue;

                    foundThisPage++;
                    boolean saved = upsertLeagueRosteredPlayer(league, playerNode, runAsOf);
                    if (saved) {
                        upsertedThisPage++;
                        totalUpserted++;
                    } else {
                        totalSkippedMissingTeam++;
                    }
                }

                if (foundThisPage == 0) {
                    log.info("No players found on page start={} for leagueKey={}, stopping", start, leagueKey);
                    break;
                }

                log.info("League {}: upserted {}/{} rostered players (start={})", leagueKey, upsertedThisPage, foundThisPage, start);

                start += pageSize;
                hasMore = foundThisPage == pageSize;

            } catch (Exception e) {
                throw new RuntimeException("Failed syncing league_rostered_player for leagueKey=" + leagueKey + " start=" + start, e);
            }
        }

        long deleted = lrpRepository.deleteByLeague_LeagueKeyAndAsOfBefore(leagueKey, runAsOf);

        log.info("League {} sync complete. upserted={}, skippedMissingTeam={}, deletedStale={}",
                leagueKey, totalUpserted, totalSkippedMissingTeam, deleted);
    }

    private String fetchLeagueRosteredPlayersJson(String leagueKey, int start, int count) {
        String accessToken = tokenService.getValidAccessToken();

        return fantasyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fantasy/v2/league/{leagueKey}/players;status=T;start={start};count={count};out=ownership")
                        .queryParam("format", "json")
                        .build(leagueKey, start, count)
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Yahoo API error: " + resp.statusCode() + " body=" + body)))
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(20))
                .block();
    }

    private boolean upsertLeagueRosteredPlayer(League league, JsonNode playerNode, Instant runAsOf) {
        JsonNode playerArray = playerNode.path(0);
        String playerId = null;
        String playerKey = null;
        String ownerTeamKey = null;

        for (JsonNode entry : playerArray) {
            if (entry == null || !entry.isObject()) continue;

            if (entry.has("player_id")) playerId = text(entry, "player_id");
            else if (entry.has("player_key")) playerKey = text(entry, "player_key");
        }

        // Ownership lives in player[1], not player[0]
        JsonNode ownershipWrapper = playerNode.path(1);
        if (!ownershipWrapper.isMissingNode() && ownershipWrapper.has("ownership")) {
            ownerTeamKey = extractOwnerTeamKey(ownershipWrapper.path("ownership"));
        }

        if (playerId == null || playerId.isBlank()) {
            return false;
        }

        if (ownerTeamKey == null || ownerTeamKey.isBlank()) {
            log.warn("Skipping playerId={} in leagueKey={} (missing ownerTeamKey)", playerId, league.getLeagueKey());
            return false;
        }

        Optional<Team> teamOpt = teamRepository.findById(ownerTeamKey);
        if (teamOpt.isEmpty()) {
            log.warn("Skipping playerId={} in leagueKey={} (team not found in DB: {})",
                    playerId, league.getLeagueKey(), ownerTeamKey);
            return false;
        }
        Team team = teamOpt.get();

        final String pid = playerId;
        final String pkey = playerKey;

        Player player = playerRepository.findById(pid).orElseGet(() -> {
            Player p = new Player();
            p.setPlayerId(pid);
            p.setPlayerKey(pkey != null ? pkey : "");
            p.setEligiblePositions(new String[0]);
            p.setIngestedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
            return playerRepository.save(p);
        });

        LeagueRosteredPlayerId id = new LeagueRosteredPlayerId(league.getLeagueKey(), player.getPlayerId());

        LeagueRosteredPlayer row = lrpRepository.findById(id).orElseGet(LeagueRosteredPlayer::new);

        row.setId(id);
        row.setLeague(league);
        row.setPlayer(player);
        row.setTeam(team);
        row.setAsOf(runAsOf);

        lrpRepository.save(row);
        return true;
    }

    /**
     * Yahoo "ownership" shape varies (object vs array wrapper).
     * This tries a few common patterns.
     */
    private String extractOwnerTeamKey(JsonNode ownershipNode) {
        if (ownershipNode == null || ownershipNode.isMissingNode() || ownershipNode.isNull()) {
            return null;
        }

        // Most common: ownership is an object with owner_team_key
        if (ownershipNode.isObject()) {
            String v = text(ownershipNode, "owner_team_key");
            if (v != null) return v;
        }

        // Sometimes: ownership comes back as an array of singleton objects
        if (ownershipNode.isArray()) {
            for (JsonNode entry : ownershipNode) {
                if (entry != null && entry.isObject()) {
                    String v = text(entry, "owner_team_key");
                    if (v != null) return v;
                }
            }
        }

        // Last resort: look one level deeper if it’s nested
        JsonNode maybe = ownershipNode.path("0");
        if (maybe.isObject()) {
            return text(maybe, "owner_team_key");
        }

        return null;
    }
}