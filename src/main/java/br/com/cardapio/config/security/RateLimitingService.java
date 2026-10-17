package br.com.cardapio.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate limit por janela fixa no Redis ({@code rate_limit:<id>}, 100 req/min).
 */
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private static final int LIMITE_REQUISICOES = 100;
    private static final Duration JANELA = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    public boolean isRequestAllowed(final String deviceId) {
        final String key = "rate_limit:" + deviceId;
        final Long requests = redisTemplate.opsForValue().increment(key);
        if (requests != null && requests == 1L) {
            redisTemplate.expire(key, JANELA);
        }
        return requests != null && requests <= LIMITE_REQUISICOES;
    }

    public Long getExpire(final String deviceId) {
        return redisTemplate.getExpire("rate_limit:" + deviceId);
    }
}
