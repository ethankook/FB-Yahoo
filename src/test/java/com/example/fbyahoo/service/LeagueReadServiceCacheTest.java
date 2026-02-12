package com.example.fbyahoo.service;

import com.example.fbyahoo.config.TestCacheConfig;
import com.example.fbyahoo.dto.api.InsightsDto;
import com.example.fbyahoo.model.League;
import com.example.fbyahoo.model.Player;
import com.example.fbyahoo.model.PlayerStats;
import com.example.fbyahoo.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {LeagueReadService.class, TestCacheConfig.class})
class LeagueReadServiceCacheTest {

    @Autowired
    private LeagueReadService leagueReadService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private LeagueRepository leagueRepository;
    @MockitoBean
    private TeamRepository teamRepository;
    @MockitoBean
    private LeagueRosteredPlayerRepository lrpRepository;
    @MockitoBean
    private PlayerStatsRepository playerStatsRepository;
    @MockitoBean
    private PlayerOwnershipRepository playerOwnershipRepository;
    @MockitoBean
    private PlayerRepository playerRepository;
    @MockitoBean
    private MatchupRepository matchupRepository;
    @MockitoBean
    private LeagueAnalyticsService leagueAnalyticsService;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void getInsights_isCached() {
        InsightsDto dto = new InsightsDto(Map.of(), List.of(), List.of());
        when(leagueAnalyticsService.computeInsights("l1")).thenReturn(dto);

        InsightsDto first = leagueReadService.getInsights("l1");
        InsightsDto second = leagueReadService.getInsights("l1");

        assertEquals(dto, first);
        assertEquals(dto, second);
        verify(leagueAnalyticsService, times(1)).computeInsights("l1");
    }

    @Test
    void getAvailablePlayers_isCached() {
        League league = League.builder().leagueKey("l1").season(2025).build();
        when(leagueRepository.findById("l1")).thenReturn(Optional.of(league));

        PlayerStats ps = PlayerStats.builder()
                .id("p1")
                .updatedAt(Instant.now())
                .build();
        when(playerStatsRepository.topAvailableByPtsPg(eq("l1"), any(PageRequest.class)))
                .thenReturn(List.of(ps));

        Player player = Player.builder()
                .playerId("p1")
                .nameFull("Player One")
                .eligiblePositions(new String[]{"PG"})
                .build();
        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));

        leagueReadService.getAvailablePlayers("l1", "pts", 10);
        leagueReadService.getAvailablePlayers("l1", "pts", 10);

        verify(leagueRepository, times(1)).findById("l1");
        verify(playerStatsRepository, times(1))
                .topAvailableByPtsPg(eq("l1"), any(PageRequest.class));
        verify(playerRepository, times(1)).findById("p1");
    }
}
