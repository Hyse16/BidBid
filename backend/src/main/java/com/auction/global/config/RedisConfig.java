package com.auction.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis 설정
 *
 * Redis는 두 가지 용도로 사용된다:
 *   1. 캐시 (@Cacheable / @CacheEvict) → RedisCacheManager
 *   2. 직접 조작 (Refresh Token 저장 등) → RedisTemplate
 *
 * 캐시 TTL 전략:
 *   - auctions (목록) : 30초 → 입찰/등록 시 즉시 evict되므로 짧게 설정
 *   - auction  (단건) : 5분  → 단건 조회는 변경 빈도가 낮아 길게 설정
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 직접 조작용 RedisTemplate
     *
     * 키는 문자열, 값은 JSON으로 직렬화하여 가독성과 호환성을 확보한다.
     * Refresh Token 저장 등 캐시 어노테이션 없이 직접 Redis를 다룰 때 사용한다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 키/값 직렬화 설정 (키: 문자열, 값: JSON)
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Spring Cache 추상화용 CacheManager
     *
     * @Cacheable, @CacheEvict 등 어노테이션 기반 캐시를 관리한다.
     * null 값은 캐싱하지 않아 불필요한 캐시 저장을 방지한다.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정 (TTL 30초, JSON 직렬화, null 캐싱 비활성화)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "auctions", defaultConfig.entryTtl(Duration.ofSeconds(30)), // 경매 목록: 30초
                "auction",  defaultConfig.entryTtl(Duration.ofMinutes(5))   // 경매 단건:  5분
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
