package com.angeltlh31.lumora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private static final int MIN_SECRET_BYTES = 32;

    static final String ISSUER = "lumora";
    static final String AUDIENCE = "lumora-web";
    static final String VERSION_CLAIM = "ver";

    private SecretKey signingKey;

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured - set the JWT_SECRET environment variable "
                            + "(generate one with: openssl rand -base64 64)");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret is too short (" + keyBytes.length + " bytes) - need at least "
                            + MIN_SECRET_BYTES + " bytes");
        }

        signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public record AccessTokenClaims(Long userId, int tokenVersion) {
    }

    public String generateToken(Long userId, int tokenVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject(String.valueOf(userId))
                .claim(VERSION_CLAIM, tokenVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public AccessTokenClaims parseAccessToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())

                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Integer version = claims.get(VERSION_CLAIM, Integer.class);
        if (version == null) {
            throw new JwtException("Access token has no version claim");
        }
        return new AccessTokenClaims(Long.valueOf(claims.getSubject()), version);
    }
}
