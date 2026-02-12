package com.example.fbyahoo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializationContext.SerializationPair<Object> serializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new LenientJsonRedisSerializer()
                );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("fbyahoo::")
                .serializeValuesWith(serializer);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error on {} key {}. Evicting and falling back to DB.",
                        cache != null ? cache.getName() : "unknown", key, exception);
                if (cache != null && key != null) {
                    try {
                        cache.evict(key);
                    } catch (Exception evictEx) {
                        log.warn("Cache evict failed for {} key {}", cache.getName(), key, evictEx);
                    }
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error on {} key {}", cache != null ? cache.getName() : "unknown", key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error on {} key {}", cache != null ? cache.getName() : "unknown", key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error on {}", cache != null ? cache.getName() : "unknown", exception);
            }
        };
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> clearCachesOnStartup(CacheManager cacheManager) {
        return event -> {
            cacheManager.getCacheNames()
                    .forEach(name -> {
                        Cache cache = cacheManager.getCache(name);
                        if (cache != null) {
                            cache.clear();
                        }
                    });
            log.info("Cleared all caches on startup");
        };
    }

    private static final class LenientJsonRedisSerializer implements RedisSerializer<Object> {
        private final GenericJackson2JsonRedisSerializer typed =
                new GenericJackson2JsonRedisSerializer();

        @Override
        public byte[] serialize(Object value) throws SerializationException {
            return typed.serialize(value);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return typed.deserialize(bytes);
            } catch (SerializationException ex) {
                // Force cache miss so CacheErrorHandler can evict bad entry
                throw ex;
            }
        }
    }
}
