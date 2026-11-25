package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.repository.RefreshTokenRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final User user = User.builder().id(7L).username("u").email("u@x").passwordHash("x").build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 60_000L);
    }

    @Test
    void createRefreshToken_storesHashNotRawToken() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertNotEquals(issued.rawToken(), saved.getValue().getToken());
        assertEquals(RefreshTokenService.hash(issued.rawToken()), saved.getValue().getToken());
        assertEquals(64, saved.getValue().getToken().length());
    }

    @Test
    void verifyAndGet_validToken_looksUpByHash() {
        RefreshToken stored = token(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken(RefreshTokenService.hash("raw"))).thenReturn(Optional.of(stored));

        assertSame(stored, refreshTokenService.verifyAndGet("raw"));
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void verifyAndGet_tokenRevokedByLogout_isTreatedAsReuse_revokesEverything() {
        RefreshToken reused = token(true, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken(RefreshTokenService.hash("stolen"))).thenReturn(Optional.of(reused));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.verifyAndGet("stolen"));

        verify(refreshTokenRepository).revokeAllByUserId(7L);
        verify(userRepository).incrementTokenVersion(7L);
    }

    @Test
    void verifyAndGet_tokenRotatedLongAgo_isTreatedAsReuse_revokesEverything() {
        RefreshToken reused = token(true, LocalDateTime.now().plusDays(1));
        reused.setRotatedAt(LocalDateTime.now().minus(RefreshTokenService.ROTATION_GRACE_PERIOD).minusSeconds(1));
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(reused));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.verifyAndGet("old"));

        verify(refreshTokenRepository).revokeAllByUserId(7L);
        verify(userRepository).incrementTokenVersion(7L);
    }

    @Test
    void verifyAndGet_tokenRotatedSecondsAgo_isAllowed_forConcurrentTabs() {
        RefreshToken justRotated = token(true, LocalDateTime.now().plusDays(1));
        justRotated.setRotatedAt(LocalDateTime.now().minusSeconds(2));
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(justRotated));

        assertSame(justRotated, refreshTokenService.verifyAndGet("raw"));
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
        verify(userRepository, never()).incrementTokenVersion(any());
    }

    @Test
    void markRotated_reusedInGrace_doesNotExtendGrace() {
        RefreshToken token = token(false, LocalDateTime.now().plusDays(1));
        refreshTokenService.markRotated(token);
        LocalDateTime firstRotation = token.getRotatedAt();
        assertTrue(token.isRevoked());

        refreshTokenService.markRotated(token);

        assertEquals(firstRotation, token.getRotatedAt());
    }

    @Test
    void verifyAndGet_expiredToken_throws() {
        RefreshToken expired = token(false, LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(expired));

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.verifyAndGet("raw"));
    }

    @Test
    void verifyAndGet_unknownToken_throws() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.verifyAndGet("nope"));
    }

    private RefreshToken token(boolean revoked, LocalDateTime expiresAt) {
        return RefreshToken.builder().user(user).token("h").revoked(revoked).expiresAt(expiresAt).build();
    }
}
