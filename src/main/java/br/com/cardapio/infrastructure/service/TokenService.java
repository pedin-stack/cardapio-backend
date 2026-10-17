package br.com.cardapio.infrastructure.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Emite e valida o JWT (HMAC256, subject = e-mail).
 */
@Service
public class TokenService {

    private static final String ISSUER = "cardapio-api";

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
}
