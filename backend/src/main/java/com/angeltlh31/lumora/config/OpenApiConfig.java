package com.angeltlh31.lumora.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Khai bao "khuon dang" cho trang Swagger UI (/swagger-ui.html) va file OpenAPI spec
// (/v3/api-docs) ma springdoc tu sinh ra tu chinh cac annotation tren Controller/DTO -
// class nay KHONG can @Bean nao ca, chi can 2 annotation cap class la du de springdoc doc duoc.
//
// @SecurityScheme: dinh nghia 1 "kieu xac thuc" ten la "bearerAuth" - noi cho Swagger UI biet
// API nay dung JWT qua header "Authorization: Bearer <token>" (scheme = "bearer", type = HTTP).
// Nho khai bao nay ma Swagger UI hien nut "Authorize" o goc phai, cho phep dan token vao 1 lan
// roi tu dong gan vao header cua MOI request test tiep theo - khong phai copy-paste tay moi lan.
//
// @OpenAPIDefinition(security = ...): ap dung "bearerAuth" nay lam mac dinh cho TAT CA endpoint.
// 2 endpoint khong can token (register, login) se duoc go bo yeu cau nay rieng bang
// @Operation(security = {}) ngay tren method do trong UserController - xem giai thich o do.
@OpenAPIDefinition(
        info = @Info(
                title = "Lumora API",
                version = "v1",
                description = "API cho Lumora - web hoc tu vung kieu Quizlet (du an hoc Spring Boot)"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {
}
