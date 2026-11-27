package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.EmailToken;
import com.angeltlh31.lumora.entity.EmailTokenPurpose;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.InvalidEmailTokenException;
import com.angeltlh31.lumora.repository.EmailTokenRepository;
import com.angeltlh31.lumora.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailTokenService {
    static final Duration TOKEN_LIFETIME = Duration.ofHours(24);

    private final EmailTokenRepository emailTokenRepository;

    public String issue(User user, EmailTokenPurpose purpose, String email) {
        emailTokenRepository.invalidateUnused(user.getId(), purpose, LocalDateTime.now());
        String rawToken = TokenHasher.newRawToken();
        emailTokenRepository.save(EmailToken.builder()
                .user(user)
                .tokenHash(TokenHasher.sha256(rawToken))
                .purpose(purpose)
                .email(email)
                .expiresAt(LocalDateTime.now().plus(TOKEN_LIFETIME))
                .build());
        return rawToken;
    }

    public EmailToken consume(String rawToken) {
        EmailToken token = emailTokenRepository.findByTokenHash(TokenHasher.sha256(rawToken))
                .orElseThrow(() -> new InvalidEmailTokenException("This link is invalid or has expired"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidEmailTokenException("This link is invalid or has expired");
        }
        token.setUsedAt(LocalDateTime.now());
        return token;
    }
}
