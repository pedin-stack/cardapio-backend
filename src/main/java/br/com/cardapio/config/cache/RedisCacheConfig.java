package br.com.cardapio.config.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Serializador JSON do cache Redis e TTL por região.
 * <p>
 * Só a região {@code userDetails} existe hoje: o cardápio público usa {@code Cache-Control} HTTP curto
 * em vez de cache no Redis (as listas imutáveis de {@code Stream.toList()} não desserializam com default typing).
 * </p>
 */
@Configuration
public class RedisCacheConfig {

    public static final String CACHE_USER_DETAILS = "userDetails";

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final Duration TTL_USUARIO = Duration.ofMinutes(2);

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        final ObjectMapper objectMapper = JsonMapper.builder()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                        ObjectMapper.DefaultTyping.EVERYTHING,
                        JsonTypeInfo.As.WRAPPER_ARRAY)
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        final GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(
                objectMapper,
                (mapper, bytes, type) -> mapper.readValue(bytes, type),
                (mapper, source) -> mapper.writerFor(Object.class).writeValueAsBytes(source));

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(final RedisCacheConfiguration baseConfig) {
        return builder -> builder.withCacheConfiguration(CACHE_USER_DETAILS, baseConfig.entryTtl(TTL_USUARIO));
    }
}
