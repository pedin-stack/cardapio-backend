package br.com.cardapio.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * A API só atende quem passa pelo front: o middleware do Next (na Vercel) injeta {@value #HEADER} com um segredo
 * compartilhado em toda chamada {@code /api/*}. Sem ele (curl, Postman, outro site) → 403, antes de rate limit e JWT.
 * <p>
 * CORS não basta: ele só restringe navegadores. {@code /videos/**} fica fora de propósito — o player do navegador
 * baixa os chunks direto da VPS (passar vídeo pela Vercel custaria banda de lá); as URLs têm UUID por envio.
 * Segredo vazio ({@code app.front.segredo}) desliga o filtro — é o caso do ambiente de dev.
 * </p>
 */
@Slf4j
@Component
public class FrontendOrigemFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Cardapio-Front";

    private final byte[] segredo;

    public FrontendOrigemFilter(@Value("${app.front.segredo}") final String segredo) {
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return segredo.length == 0 || !request.getServletPath().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {
        final String recebido = request.getHeader(HEADER);
        // Comparação em tempo constante: não vaza, pelo tempo de resposta, quantos caracteres acertou.
        if (recebido != null && MessageDigest.isEqual(segredo, recebido.getBytes(StandardCharsets.UTF_8))) {
            filterChain.doFilter(request, response);
            return;
        }
        log.warn("Chamada à API fora do front recusada: {} {}", request.getMethod(), request.getServletPath());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\": 403, \"message\": \"Acesso permitido somente pelo aplicativo do cardápio.\"}");
    }
}
