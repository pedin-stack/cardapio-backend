package br.com.cardapio.infrastructure.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Emite e valida o JWT (HMAC256): a sessão do gestor (subject = e-mail) e o token do cliente do cardápio público.
 */
@Service
public class TokenService {

    private static final String ISSUER = "cardapio-api";
    private static final String AUDIENCIA_CLIENTE = "cliente";
    /** O cliente fica identificado naquele navegador por um ano (não há senha para pedir de novo). */
    private static final long VALIDADE_CLIENTE_DIAS = 365;

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration-hours}")
    private Long expirationHours;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        algorithm = Algorithm.HMAC256(secretKey);
    }

    /** Instante de expiração de um token emitido agora. */
    public Instant generateExpiration() {
        return Instant.now().plus(expirationHours, ChronoUnit.HOURS);
    }

    public String generateToken(final String email, final Instant expiraEm) {
        return JWT.create()
                .withSubject(email)
                .withIssuer(ISSUER)
                .withIssuedAt(Instant.now())
                .withExpiresAt(expiraEm)
                .sign(algorithm);
    }

    /**
     * @return o e-mail (subject) se o token for válido, ou {@code null} caso contrário.
     */
    public String validateToken(final String token) {
        try {
            return JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject();
        } catch (final JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * Token do cliente identificado no cardápio público (subject = id do cliente). A audiência própria impede que
     * o token de sessão do gestor valha aqui (e vice-versa: no JWTLoginFilter o subject não é um e-mail cadastrado).
     */
    public String generateTokenCliente(final UUID clienteId) {
        return JWT.create()
                .withSubject(clienteId.toString())
                .withIssuer(ISSUER)
                .withAudience(AUDIENCIA_CLIENTE)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(VALIDADE_CLIENTE_DIAS, ChronoUnit.DAYS))
                .sign(algorithm);
    }

    /**
     * @return o id do cliente se o token for válido e de cliente, ou {@code null} caso contrário.
     */
    public UUID validateTokenCliente(final String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(JWT.require(algorithm).withIssuer(ISSUER).withAudience(AUDIENCIA_CLIENTE).build()
                    .verify(token).getSubject());
        } catch (final JWTVerificationException | IllegalArgumentException exception) {
            return null;
        }
    }
}
