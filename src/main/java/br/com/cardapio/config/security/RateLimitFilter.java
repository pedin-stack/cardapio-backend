package br.com.cardapio.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limit executado antes do JWT. Identifica o cliente por {@code X-Device-ID} (o cardápio público envia o
 * dispositivoId) ou, na ausência, pelo IP. ⚠️ O header é controlado pelo cliente: não é proteção contra DDoS.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getServletPath();
        // /videos: o player pede dezenas de chunks por minuto e sem X-Device-ID; contado por IP, um wi-fi de
        // restaurante (todos atrás do mesmo NAT) estouraria o limite e travaria os vídeos de todo mundo.
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/videos/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {

        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId == null || deviceId.isBlank() || deviceId.length() > 64) {
            deviceId = request.getRemoteAddr();
        }

        if (rateLimitingService.isRequestAllowed(deviceId)) {
            filterChain.doFilter(request, response);
            return;
        }

        final Long retryAfter = rateLimitingService.getExpire(deviceId);
        if (retryAfter != null && retryAfter > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfter));
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\": 429, \"message\": \"Limite de requisições excedido. Tente novamente mais tarde.\"}");
    }
}
