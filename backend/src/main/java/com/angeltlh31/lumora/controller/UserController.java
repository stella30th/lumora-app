package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.security.RefreshTokenCookie;
import org.springframework.http.HttpHeaders;
import com.angeltlh31.lumora.dto.MessageResponse;
import com.angeltlh31.lumora.dto.user.ChangePasswordRequest;
import com.angeltlh31.lumora.dto.user.ResendVerificationRequest;
import com.angeltlh31.lumora.dto.user.VerifyEmailRequest;
import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.UpdateProfileRequest;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
import com.angeltlh31.lumora.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RefreshTokenCookie refreshTokenCookie;

    @Operation(summary = "Dang ky user moi - luon tra ve cung 1 thong bao, gui link xac thuc qua email", security = {})
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.registerUser(request));
    }

    @Operation(summary = "Xac thuc email bang token trong link da gui", security = {})
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyEmail(request.getToken()));
    }

    @Operation(summary = "Gui lai link xac thuc email", security = {})
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.resendVerification(request.getEmail()));
    }

    @Operation(summary = "Dang nhap, tra ve access token (JWT) trong body + refresh token trong cookie httpOnly",
            security = {})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(userService.login(request));
    }

    @Operation(summary = "Doi refresh token (doc tu cookie httpOnly) lay access token moi + xoay vong refresh token",
            security = {})
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Missing refresh token - please login again");
        }
        return withRefreshCookie(userService.refreshAccessToken(rawRefreshToken));
    }

    @Operation(summary = "Dang xuat - thu hoi toan bo refresh token cua user dang dang nhap va xoa cookie")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .build();
    }

    private ResponseEntity<LoginResponse> withRefreshCookie(UserService.AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.rawRefreshToken()).toString())
                .body(result.body());
    }

    @Operation(summary = "Xem thong tin cua chinh user dang dang nhap")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(summary = "Cap nhat username/email cua chinh user dang dang nhap")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Long userId,
                                                        @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @Operation(summary = "Doi mat khau cua chinh user dang dang nhap")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
