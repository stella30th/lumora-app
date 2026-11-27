package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.MessageResponse;
import com.angeltlh31.lumora.dto.user.ChangePasswordRequest;
import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.UpdateProfileRequest;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
import com.angeltlh31.lumora.entity.EmailToken;
import com.angeltlh31.lumora.entity.EmailTokenPurpose;
import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.DuplicateResourceException;
import com.angeltlh31.lumora.exception.EmailNotVerifiedException;
import com.angeltlh31.lumora.exception.InvalidCredentialException;
import com.angeltlh31.lumora.exception.InvalidEmailTokenException;
import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.mail.AccountMailer;
import com.angeltlh31.lumora.repository.UserRepository;
import com.angeltlh31.lumora.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    static final String REGISTER_MESSAGE =
            "If this email can be used, we have sent a verification link to it. Check your inbox to finish signing up.";
    static final String RESEND_MESSAGE =
            "If this email belongs to an unverified account, we have sent a new verification link.";
    private static final String INVALID_LINK = "This link is invalid or has expired";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailTokenService emailTokenService;
    private final AccountMailer accountMailer;

    private volatile String dummyPasswordHash;

    public MessageResponse registerUser(UserRegisterRequest request) {
        String username = request.getUsername().trim();
        String email = normalizeEmail(request.getEmail());
        String passwordHash = passwordEncoder.encode(request.getPassword());

        Optional<User> byEmail = userRepository.findByEmail(email);
        Optional<User> byUsername = userRepository.findByUsername(username);

        boolean usernameAvailable = byUsername.isEmpty()
                || (byEmail.isPresent()
                && !byEmail.get().isEmailVerified()
                && byUsername.get().getId().equals(byEmail.get().getId()));
        if (!usernameAvailable) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }

        if (byEmail.isEmpty()) {
            User user = userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .build());
            sendVerification(user);
        } else if (byEmail.get().isEmailVerified()) {
            accountMailer.sendAlreadyRegistered(email);
        } else {
            User unverified = byEmail.get();
            unverified.setUsername(username);
            unverified.setPasswordHash(passwordHash);
            sendVerification(unverified);
        }
        return new MessageResponse(REGISTER_MESSAGE);
    }

    public MessageResponse resendVerification(String rawEmail) {
        userRepository.findByEmail(normalizeEmail(rawEmail))
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerification);
        return new MessageResponse(RESEND_MESSAGE);
    }

    @Transactional(noRollbackFor = DuplicateResourceException.class)
    public MessageResponse verifyEmail(String rawToken) {
        EmailToken token = emailTokenService.consume(rawToken);
        User user = token.getUser();

        if (token.getPurpose() == EmailTokenPurpose.VERIFY_EMAIL) {
            if (!token.getEmail().equals(user.getEmail())) {
                throw new InvalidEmailTokenException(INVALID_LINK);
            }
            user.setEmailVerified(true);
            return new MessageResponse("Your email is verified. You can log in now.");
        }

        if (!token.getEmail().equals(user.getPendingEmail())) {
            throw new InvalidEmailTokenException(INVALID_LINK);
        }
        if (userRepository.existsByEmailAndIdNot(token.getEmail(), user.getId())) {
            user.setPendingEmail(null);
            throw new DuplicateResourceException("This email is already used by another account");
        }
        user.setEmail(token.getEmail());
        user.setPendingEmail(null);
        user.setEmailVerified(true);
        return new MessageResponse("Your new email is confirmed.");
    }

    public record AuthResult(LoginResponse body, String rawRefreshToken) {
    }

    public AuthResult login(LoginRequest request) {
        Optional<User> found = userRepository.findByEmail(normalizeEmail(request.getEmail()));
        if (found.isEmpty()) {
            passwordEncoder.matches(request.getPassword(), dummyPasswordHash());
            throw new InvalidCredentialException("Invalid email or password");
        }
        User user = found.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialException("Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }
        return issueTokenPair(user);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResult refreshAccessToken(String rawRefreshToken) {
        RefreshToken oldToken = refreshTokenService.verifyAndGet(rawRefreshToken);
        User user = oldToken.getUser();

        refreshTokenService.markRotated(oldToken);

        return issueTokenPair(user);
    }

    public void logout(Long userId) {
        revokeAllSessions(userId);
    }

    private void revokeAllSessions(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
        userRepository.incrementTokenVersion(userId);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);
        String username = request.getUsername().trim();
        String newEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        user.setUsername(username);

        if (newEmail.equals(user.getEmail())) {
            user.setPendingEmail(null);
        } else if (!newEmail.equals(user.getPendingEmail())) {
            user.setPendingEmail(newEmail);
            String rawToken = emailTokenService.issue(user, EmailTokenPurpose.CHANGE_EMAIL, newEmail);
            accountMailer.sendChangeEmailConfirmation(newEmail, rawToken);
            accountMailer.sendEmailChangeRequestedNotice(user.getEmail(), newEmail);
        }

        return toResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        revokeAllSessions(userId);
    }

    private void sendVerification(User user) {
        String rawToken = emailTokenService.issue(user, EmailTokenPurpose.VERIFY_EMAIL, user.getEmail());
        accountMailer.sendVerification(user.getEmail(), rawToken);
    }

    private String dummyPasswordHash() {
        if (dummyPasswordHash == null) {
            dummyPasswordHash = passwordEncoder.encode("lumora-timing-equalizer");
        }
        return dummyPasswordHash;
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id=" + id));
    }

    private AuthResult issueTokenPair(User user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getTokenVersion());
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        LoginResponse body = LoginResponse.builder()
                .token(accessToken)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
        return new AuthResult(body, refreshToken.rawToken());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .pendingEmail(user.getPendingEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
