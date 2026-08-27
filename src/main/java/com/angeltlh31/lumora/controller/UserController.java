package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
import com.angeltlh31.lumora.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Dang nhap, tra ve JWT de dung cho cac endpoint con lai", security = {})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
