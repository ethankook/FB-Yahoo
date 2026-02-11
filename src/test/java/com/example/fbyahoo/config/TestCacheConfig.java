package com.example.fbyahoo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CacheNames.LEAGUE_LIST,
                CacheNames.LEAGUE_DETAIL,
                CacheNames.LEAGUE_ROSTER,
                CacheNames.LEAGUE_AVAILABLE,
                CacheNames.LEAGUE_STANDINGS,
                CacheNames.LEAGUE_MATCHUP,
                CacheNames.LEAGUE_INSIGHTS
        );
    }
}
