package com.pizzaria.backendpizzaria.config;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

@Configuration
public class RedisCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    private final RedisProperties redisProperties;

    public RedisCacheConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = buildRedisConfiguration();
        log.info("Connecting to Redis server at {}:{}", configuration.getHostName(), configuration.getPort());
        return new LettuceConnectionFactory(configuration);
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

    private RedisStandaloneConfiguration buildRedisConfiguration() {
        if (StringUtils.hasText(redisProperties.getUrl())) {
            return buildFromUrl(redisProperties.getUrl());
        }
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        configuration.setDatabase(redisProperties.getDatabase());
        if (StringUtils.hasText(redisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        return configuration;
    }

    private RedisStandaloneConfiguration buildFromUrl(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 6379 : uri.getPort();
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);

        String userInfo = uri.getUserInfo();
        if (StringUtils.hasText(userInfo)) {
            String[] parts = userInfo.split(":", 2);
            String password = parts.length == 2 ? parts[1] : parts[0];
            if (StringUtils.hasText(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
        }

        String path = uri.getPath();
        if (StringUtils.hasText(path) && path.length() > 1) {
            try {
                configuration.setDatabase(Integer.parseInt(path.substring(1)));
            } catch (NumberFormatException e) {
                log.warn("Invalid Redis database in REDIS_URL '{}'", path);
            }
        }
        return configuration;
    }
}
