package com.angeltlh31.lumora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private JwtService serviceWithSecret(String secret) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "expirationMs", 60_000L);
        return service;
    }

    @Test
    void missingSecret_failsAtStartup() {
        assertThrows(IllegalStateException.class, () -> serviceWithSecret("").validateSecret());
        assertThrows(IllegalStateException.class, () -> serviceWithSecret(null).validateSecret());
    }

    @Test
    void tooShortSecret_failsAtStartup() {
        assertThrows(IllegalStateException.class, () -> serviceWithSecret("short-secret").validateSecret());
    }

    @Test
    void validSecret_tokenRoundTrips() {
        JwtService service = serviceWithSecret("a-test-secret-that-is-definitely-longer-than-32-bytes");
        service.validateSecret();

        String token = service.generateToken(42L, 3);

        JwtService.AccessTokenClaims claims = service.parseAccessToken(token);
        assertEquals(42L, claims.userId());
        assertEquals(3, claims.tokenVersion());
    }

    @Test
    void tokenSignedWithOtherSecret_isRejected() {
        JwtService attacker = serviceWithSecret("attacker-secret-attacker-secret-attacker-secret");
        attacker.validateSecret();
        JwtService server = serviceWithSecret("server-secret-server-secret-server-secret-server");
        server.validateSecret();

        String forged = attacker.generateToken(1L, 0);

        assertThrows(JwtException.class, () -> server.parseAccessToken(forged));
    }

    @Test
    void tokenWithWrongIssuerOrAudience_isRejected_evenWithCorrectSignature() {
        String secret = "server-secret-server-secret-server-secret-server";
        JwtService server = serviceWithSecret(secret);
        server.validateSecret();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String otherIssuer = Jwts.builder().issuer("someone-else").audience().add(JwtService.AUDIENCE).and()
                .subject("1").claim(JwtService.VERSION_CLAIM, 0).signWith(key).compact();
        String otherAudience = Jwts.builder().issuer(JwtService.ISSUER).audience().add("mobile").and()
                .subject("1").claim(JwtService.VERSION_CLAIM, 0).signWith(key).compact();
        String noVersion = Jwts.builder().issuer(JwtService.ISSUER).audience().add(JwtService.AUDIENCE).and()
                .subject("1").signWith(key).compact();

        assertThrows(JwtException.class, () -> server.parseAccessToken(otherIssuer));
        assertThrows(JwtException.class, () -> server.parseAccessToken(otherAudience));
        assertThrows(JwtException.class, () -> server.parseAccessToken(noVersion));
    }

    @Test
    void tokenNoLongerCarriesUsernameClaim() {
        JwtService service = serviceWithSecret("a-test-secret-that-is-definitely-longer-than-32-bytes");
        service.validateSecret();
        SecretKey key = Keys.hmacShaKeyFor(
                "a-test-secret-that-is-definitely-longer-than-32-bytes".getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(service.generateToken(5L, 0)).getPayload();

        assertNull(claims.get("username"));
    }
}
