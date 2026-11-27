package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.repository.RefreshTokenRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import com.angeltlh31.lumora.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    static final Duration ROTATION_GRACE_PERIOD = Duration.ofSeconds(30);

    public String createRefreshToken(User user) {
        String rawToken = TokenHasher.newRawToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(TokenHasher.sha256(rawToken))
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build());
        return rawToken;
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RefreshToken verifyAndGet(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(TokenHasher.sha256(rawToken))
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
}
