package com.angeltlh31.lumora.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookie {
    public static final String NAME = "lumora_refresh";
    private static final String PATH = "/api/users";

    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public RefreshTokenCookie(@Value("${auth.refresh-cookie.secure:false}") boolean secure,
                              @Value("${auth.refresh-cookie.same-site:Lax}") String sameSite,
                              @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAge = Duration.ofMillis(refreshExpirationMs);
    }

    public ResponseCookie create(String rawToken) {
        return base(rawToken).maxAge(maxAge).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(PATH);
    }
}
