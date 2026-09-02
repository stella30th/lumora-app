package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.RefreshTokenRequest;
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

    // security = {} : go bo yeu cau "bearerAuth" ma OpenApiConfig da dat lam MAC DINH cho
    // toan bo API. 2 endpoint nay la ngoai le duy nhat - chinh no la noi TAO ra token/tao ra
    // user, nen luc goi chua the co token nao ca. Neu khong khai bao, Swagger UI van hien
    // o khoa "yeu cau dang nhap" nham tren 2 nut nay, gay hieu lam trong luc test.
    @Operation(summary = "Dang ky user moi", security = {})
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Dang nhap, tra ve access token (JWT) + refresh token", security = {})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // Ngay 15: security = {} GIONG register/login, ly do KHAC HAN - endpoint nay ton tai
    // CHINH VI access token da HET HAN (khong con dung duoc de gan header Authorization nua),
    // nen bat buoc access token phai KHONG can thiet o day. Viec "xac thuc" o endpoint nay
    // hoan toan dua vao chinh refreshToken trong body (kiem tra boi UserService/
    // RefreshTokenService), khong dua vao SecurityFilterChain nhu moi endpoint con lai.
    @Operation(summary = "Doi refresh token con hop le lay 1 cap access+refresh token moi",
            security = {})
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshAccessToken(request.getRefreshToken()));
    }

    // Nguoc voi refresh(): endpoint nay CAN access token hop le (khong khai bao security = {})
    // - phai biet chinh xac "ai" dang logout de biet thu hoi refresh token cua AI.
    // @AuthenticationPrincipal Long userId lay tu chinh JWT dang dung, giong het DeckController.
    @Operation(summary = "Dang xuat - thu hoi toan bo refresh token cua user dang dang nhap")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xem thong tin user theo id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
