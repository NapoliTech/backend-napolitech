package com.pizzaria.backendpizzaria.config;

import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "redis");
        String portStr = System.getenv().getOrDefault("REDIS_PORT", "6379");
        int port = 6379;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid REDIS_PORT '{}', falling back to 6379", portStr);
        }

        log.info("Connecting to Redis server at {}:{}", host, port);
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {

        log.info("Cache type: redis - building RedisCacheManager");

        var json = new GenericJackson2JsonRedisSerializer();

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(json))
                .disableCachingNullValues()
                .prefixCacheNameWith("app::")
                .entryTtl(Duration.ofMinutes(5)); // TTL padrão de 5 minutos

        // Configurações específicas por cache
        var perCache = Map.of(
                "produtoPorId", defaults.entryTtl(Duration.ofMinutes(10)),
                "listaProdutos", defaults.entryTtl(Duration.ofMinutes(5))
        );

        var manager = RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .build();

        log.info("RedisCacheManager built and registered as primary cacheManager");
        return manager;
    }
}