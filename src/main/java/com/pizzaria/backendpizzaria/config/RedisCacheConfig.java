package com.pizzaria.backendpizzaria.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        var json = new GenericJackson2JsonRedisSerializer(buildObjectMapper());

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(json))
                .disableCachingNullValues()
                .prefixCacheNameWith("app::")
                .entryTtl(Duration.ofMinutes(5));

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

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        mapper.activateDefaultTypingAsProperty(ptv, ObjectMapper.DefaultTyping.NON_FINAL, "@class");

        mapper.registerModule(new SimpleModule()
                .addDeserializer(PageImpl.class, new PageImplDeserializer()));

        return mapper;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class PageImplDeserializer extends StdDeserializer<PageImpl> {

        PageImplDeserializer() {
            super(PageImpl.class);
        }

        @Override
        public PageImpl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectNode node = p.getCodec().readTree(p);

            int number       = node.path("number").asInt(0);
            int size         = node.path("size").asInt(20);
            long totalElements = node.path("totalElements").asLong(0);

            List<Object> content = new ArrayList<>();
            JsonNode contentNode = node.get("content");
            if (contentNode != null && contentNode.isArray()) {
                for (JsonNode item : contentNode) {
                    JsonNode classNode = item.get("@class");
                    if (classNode != null) {
                        try {
                            Class<?> clazz = Class.forName(classNode.asText());
                            content.add(p.getCodec().treeToValue(item, clazz));
                        } catch (ClassNotFoundException ex) {
                            content.add(p.getCodec().treeToValue(item, Object.class));
                        }
                    } else {
                        content.add(p.getCodec().treeToValue(item, Object.class));
                    }
                }
            }

            return new PageImpl<>(content, PageRequest.of(number, Math.max(size, 1)), totalElements);
        }
    }
}
