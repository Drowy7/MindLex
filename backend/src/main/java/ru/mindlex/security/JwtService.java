package ru.mindlex.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import ru.mindlex.config.MindLexProperties;
import ru.mindlex.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Выпуск и проверка JWT. Токен самодостаточен, поэтому серверу не нужно
 * хранить сессии — это ключевое условие работы нескольких экземпляров (фактор VI).
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final java.time.Duration ttl;
    private final Clock clock;

    public JwtService(MindLexProperties properties, Clock clock) {
        this.clock = clock;
        this.issuer = properties.jwt().issuer();
        this.ttl = properties.jwt().ttl();
        byte[] secret = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("MINDLEX_JWT_SECRET должен быть не короче 32 байт");
        }
        this.key = Keys.hmacShaKeyFor(secret);
    }

    public String issue(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);
            return Optional.of(new AuthenticatedUser(userId, claims.get("email", String.class),
                    role == null ? "ROLE_USER" : role));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}
