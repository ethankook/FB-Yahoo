package com.example.fbyahoo.controller.api;

import com.example.fbyahoo.config.CacheNames;
import com.example.fbyahoo.config.TestCacheConfig;
import com.example.fbyahoo.service.ingestion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {SyncApiController.class, TestCacheConfig.class})
class SyncEvictsCachesTest {

    @Autowired
    private SyncApiController syncApiController;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private LeagueIngestionService leagueIngestionService;
    @MockitoBean
    private TeamIngestionService teamIngestionService;
    @MockitoBean
    private PlayerIngestionService playerIngestionService;
    @MockitoBean
    private PlayerOwnershipIngestionService playerOwnershipIngestionService;
    @MockitoBean
    private LeagueRosteredPlayerIngestionService leagueRosteredPlayerIngestionService;
    @MockitoBean
    private PlayerStatsIngestionService playerStatsIngestionService;
    @MockitoBean
    private StandingsIngestionService standingsIngestionService;
    @MockitoBean
    private MatchupIngestionService matchupIngestionService;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void syncEvictsAllCaches() {
        cacheManager.getCache(CacheNames.LEAGUE_LIST).put("key", "value");
        cacheManager.getCache(CacheNames.LEAGUE_DETAIL).put("l1", "value");
        cacheManager.getCache(CacheNames.LEAGUE_ROSTER).put("l1", "value");
        cacheManager.getCache(CacheNames.LEAGUE_AVAILABLE).put("l1:pts:10", "value");
        cacheManager.getCache(CacheNames.LEAGUE_STANDINGS).put("l1", "value");
        cacheManager.getCache(CacheNames.LEAGUE_MATCHUP).put("l1:1", "value");
        cacheManager.getCache(CacheNames.LEAGUE_INSIGHTS).put("l1", "value");

        syncApiController.sync();

        assertNull(cacheManager.getCache(CacheNames.LEAGUE_LIST).get("key"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_DETAIL).get("l1"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_ROSTER).get("l1"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_AVAILABLE).get("l1:pts:10"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_STANDINGS).get("l1"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_MATCHUP).get("l1:1"));
        assertNull(cacheManager.getCache(CacheNames.LEAGUE_INSIGHTS).get("l1"));
    }
}
