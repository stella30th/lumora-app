package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.MessageResponse;
import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.UpdateProfileRequest;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
import com.angeltlh31.lumora.entity.EmailToken;
import com.angeltlh31.lumora.entity.EmailTokenPurpose;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.DuplicateResourceException;
import com.angeltlh31.lumora.exception.EmailNotVerifiedException;
import com.angeltlh31.lumora.exception.InvalidCredentialException;
import com.angeltlh31.lumora.exception.InvalidEmailTokenException;
import com.angeltlh31.lumora.mail.AccountMailer;
import com.angeltlh31.lumora.repository.UserRepository;
import com.angeltlh31.lumora.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmailTokenService emailTokenService;
    @Mock private AccountMailer accountMailer;

    @InjectMocks
    private UserService userService;

    private UserRegisterRequest registerRequest(String username, String email) {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("Password123");
        return request;
    }

    private User user(long id, String username, String email, boolean verified) {
        return User.builder().id(id).username(username).email(email).passwordHash("hash")
                .emailVerified(verified).build();
    }

    @Test
    void register_newEmail_createsUnverifiedUserAndSendsVerification() {
        when(passwordEncoder.encode("Password123")).thenReturn("hash");
        when(userRepository.findByEmail("alice@test.local")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailTokenService.issue(any(), eq(EmailTokenPurpose.VERIFY_EMAIL), eq("alice@test.local")))
                .thenReturn("raw-token");

        MessageResponse response = userService.registerUser(registerRequest("alice", "  Alice@Test.LOCAL "));

        assertEquals(UserService.REGISTER_MESSAGE, response.message());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("alice@test.local", saved.getValue().getEmail());
        assertFalse(saved.getValue().isEmailVerified());
        verify(accountMailer).sendVerification("alice@test.local", "raw-token");
    }

    @Test
    void register_emailOfVerifiedAccount_returnsSameMessage_andOnlyNotifiesOwner() {
        User existing = user(1, "owner", "alice@test.local", true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.findByEmail("alice@test.local")).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());

        MessageResponse response = userService.registerUser(registerRequest("newname", "alice@test.local"));

        assertEquals(UserService.REGISTER_MESSAGE, response.message());
        verify(accountMailer).sendAlreadyRegistered("alice@test.local");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailTokenService);
    }

    @Test
    void register_alwaysHashesPassword_soResponseTimeDoesNotRevealExistingEmail() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user(1, "owner", "alice@test.local", true)));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        userService.registerUser(registerRequest("newname", "alice@test.local"));

        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void register_takenUsername_isRejected_evenWhenEmailBelongsToItsVerifiedOwner() {
        User owner = user(1, "alice", "alice@test.local", true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.findByEmail("alice@test.local")).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(owner));

        assertThrows(DuplicateResourceException.class,
                () -> userService.registerUser(registerRequest("alice", "alice@test.local")));
    }

    @Test
    void register_unverifiedAccount_isTakenOverBySecondSignup_andGetsNewLink() {
        User squatter = user(1, "squat", "alice@test.local", false);
        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
        when(userRepository.findByEmail("alice@test.local")).thenReturn(Optional.of(squatter));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(emailTokenService.issue(squatter, EmailTokenPurpose.VERIFY_EMAIL, "alice@test.local"))
                .thenReturn("raw");

        userService.registerUser(registerRequest("alice", "alice@test.local"));

        assertEquals("alice", squatter.getUsername());
        assertEquals("new-hash", squatter.getPasswordHash());
        verify(accountMailer).sendVerification("alice@test.local", "raw");
    }

    @Test
    void login_unknownEmail_stillRunsPasswordCheck_andFailsGenerically() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@test.local");
        request.setPassword("whatever1");
        when(userRepository.findByEmail("nobody@test.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");

        assertThrows(InvalidCredentialException.class, () -> userService.login(request));
        verify(passwordEncoder).matches("whatever1", "dummy-hash");
    }

    @Test
    void login_correctPasswordButUnverified_isRejected() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@test.local");
        request.setPassword("Password123");
        when(userRepository.findByEmail("alice@test.local"))
                .thenReturn(Optional.of(user(1, "alice", "alice@test.local", false)));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class, () -> userService.login(request));
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void verifyEmail_marksUserVerified() {
        User alice = user(1, "alice", "alice@test.local", false);
        when(emailTokenService.consume("raw")).thenReturn(EmailToken.builder()
                .user(alice).purpose(EmailTokenPurpose.VERIFY_EMAIL).email("alice@test.local").build());

        userService.verifyEmail("raw");

        assertTrue(alice.isEmailVerified());
    }

    @Test
    void updateProfile_newEmail_staysPendingUntilConfirmed_andDoesNotRevealIfTaken() {
        User alice = user(1, "alice", "alice@test.local", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(emailTokenService.issue(alice, EmailTokenPurpose.CHANGE_EMAIL, "taken@test.local")).thenReturn("raw");
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("alice");
        request.setEmail("taken@test.local");

        UserResponse response = userService.updateProfile(1L, request);

        assertEquals("alice@test.local", response.getEmail());
        assertEquals("taken@test.local", response.getPendingEmail());
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(accountMailer).sendChangeEmailConfirmation("taken@test.local", "raw");
        verify(accountMailer).sendEmailChangeRequestedNotice("alice@test.local", "taken@test.local");
    }

    @Test
    void verifyChangeEmail_appliesNewEmail() {
        User alice = user(1, "alice", "alice@test.local", true);
        alice.setPendingEmail("new@test.local");
        when(emailTokenService.consume("raw")).thenReturn(EmailToken.builder()
                .user(alice).purpose(EmailTokenPurpose.CHANGE_EMAIL).email("new@test.local").build());
        when(userRepository.existsByEmailAndIdNot("new@test.local", 1L)).thenReturn(false);

        userService.verifyEmail("raw");

        assertEquals("new@test.local", alice.getEmail());
        assertNull(alice.getPendingEmail());
    }

    @Test
    void verifyChangeEmail_withOutdatedLink_isRejected() {
        User alice = user(1, "alice", "alice@test.local", true);
        alice.setPendingEmail("newer@test.local");
        when(emailTokenService.consume("raw")).thenReturn(EmailToken.builder()
                .user(alice).purpose(EmailTokenPurpose.CHANGE_EMAIL).email("older@test.local").build());

        assertThrows(InvalidEmailTokenException.class, () -> userService.verifyEmail("raw"));
        assertEquals("alice@test.local", alice.getEmail());
    }
}
