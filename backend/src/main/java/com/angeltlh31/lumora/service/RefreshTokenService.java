package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.repository.RefreshTokenRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static final Duration ROTATION_GRACE_PERIOD = Duration.ofSeconds(30);

    public record IssuedRefreshToken(RefreshToken entity, String rawToken) {
    }

    public IssuedRefreshToken createRefreshToken(User user) {
        String rawToken = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)

                .token(hash(rawToken))
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build();
        return new IssuedRefreshToken(refreshTokenRepository.save(refreshToken), rawToken);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshToken verifyAndGet(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked() && !isWithinRotationGrace(refreshToken)) {
            Long userId = refreshToken.getUser().getId();
            log.warn("Revoked refresh token reused for userId={} - revoking all sessions", userId);
            revokeAllForUser(userId);
            userRepository.incrementTokenVersion(userId);
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked - please login again");
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has expired - please login again");
        }
        return refreshToken;
    }

    public void markRotated(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        if (refreshToken.getRotatedAt() == null) {
            refreshToken.setRotatedAt(LocalDateTime.now());
        }
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private boolean isWithinRotationGrace(RefreshToken token) {
        return token.getRotatedAt() != null
                && token.getRotatedAt().isAfter(LocalDateTime.now().minus(ROTATION_GRACE_PERIOD));
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
