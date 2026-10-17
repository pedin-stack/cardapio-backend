package br.com.cardapio.config.security;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.ErrorResponse;
import br.com.cardapio.infrastructure.service.TokenService;
import br.com.cardapio.infrastructure.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * Filtro JWT executado a cada requisição.
 * <p>
 * Header {@code Authorization: Bearer} tem precedência (Swagger/integrações); o cookie HttpOnly
 * {@value #TOKEN_COOKIE_NAME} é o caminho do front. Token do header inválido → 401; token do cookie
 * inválido → segue anônimo (o JS não apaga cookie HttpOnly, e um 401 trancaria as rotas públicas).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTLoginFilter extends OncePerRequestFilter {

    public static final String TOKEN_COOKIE_NAME = "cardapio_token";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
                                    @NonNull final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {

        final boolean tokenDoHeader = extrairDoHeader(request).isPresent();
        try {
            final Optional<String> token = extrairDoHeader(request).or(() -> extrairDoCookie(request));
            if (token.isPresent()) {
                final String email = tokenService.validateToken(token.get());
                final UsuarioUserDetailsDTO usuario = email != null ? userDetailsService.loadUserByUsername(email) : null;

                if (usuario != null && usuario.isEnabled()) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
                } else if (tokenDoHeader) {
                    escreverErro401(response, "Token JWT inválido ou expirado.");
                    return;
                } else {
                    log.debug("Cookie de sessão inválido ou usuário desabilitado na rota {}", request.getServletPath());
                }
            }
        } catch (final UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            log.warn("Falha na autenticação JWT: {}", ex.getMessage());
            if (tokenDoHeader) {
                escreverErro401(response, "Falha na autenticação JWT.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static Optional<String> extrairDoHeader(final HttpServletRequest request) {
        final String header = request.getHeader(AUTH_HEADER);
        return header != null && header.startsWith(BEARER_PREFIX)
                ? Optional.of(header.substring(BEARER_PREFIX.length()))
                : Optional.empty();
    }

    private static Optional<String> extrairDoCookie(final HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> TOKEN_COOKIE_NAME.equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .filter(StringUtils::hasText)
                        .findFirst());
    }

    private void escreverErro401(final HttpServletResponse response, final String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        OBJECT_MAPPER.writeValue(response.getWriter(), new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, mensagem));
    }
}
