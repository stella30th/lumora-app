package com.angeltlh31.lumora.config;

import com.angeltlh31.lumora.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring tu tim va inject bean JwtAuthenticationFilter (da danh dau @Component) vao day -
    // khong can khai bao @Bean rieng cho no, chi can no ton tai trong Spring context.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Bean dung chung cho ca app: hash password luc register, so khop luc login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // QUAN TRONG: chi co starter-security tren classpath la Spring da tu dong khoa
    // TOAN BO endpoint bang HTTP Basic Auth mac dinh (sinh 1 password ngau nhien in ra console).
    // Filter chain ben duoi ghi de lai hanh vi mac dinh do.
    //
    // Buoc 5 (JWT Filter) sua lai placeholder cua ngay 5: gio moi request bat buoc phai co
    // token hop le, TRU 2 endpoint permitAll ben duoi.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF la co che bao ve session/cookie cua trinh duyet - REST API dung token
                // (khong dung cookie de xac thuc) nen khong can, tat di cho don gian.
                .csrf(csrf -> csrf.disable())
                // STATELESS: bao Spring KHONG tao/luu HttpSession cho user.
                // Dung tinh than cua JWT - moi request tu mang du thong tin xac thuc trong token,
                // server khong nho "ai vua dang nhap" giua cac request.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sieu lai placeholder permitAll() cua ngay 5: chi 2 endpoint nay khong can token
                // (vi chinh no la noi TAO ra token / tao ra user) - moi request khac deu phai
                // duoc JwtAuthenticationFilter xac thuc thanh cong truoc do.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                // Swagger UI + OpenAPI spec: phai permitAll, khong thi chinh
                                // JwtAuthenticationFilter/authenticated() ben duoi se chan luon
                                // trang Swagger (403/401) truoc khi ban kip nhap token vao.
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                // addFilterBefore: chen JwtAuthenticationFilter vao TRUOC
                // UsernamePasswordAuthenticationFilter (filter mac dinh cua Spring Security lo
                // xu ly form login) trong chuoi filter. Vi tri nay dam bao SecurityContext da
                // duoc "dien" userId (neu token hop le) TRUOC KHI .authorizeHttpRequests() ben
                // tren kiem tra .authenticated().
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // exceptionHandling: neu .authenticated() o tren that bai (khong co / sai token),
                // Spring Security se goi authenticationEntryPoint nay thay vi trang loi mac dinh
                // (403 rong, kho debug). Tra ve JSON 401 cung dinh dang voi GlobalExceptionHandler
                // cho de doc luc test bang curl/Postman.
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                            "{\"status\":401,\"message\":\"Thieu token hoac token khong hop le\"}");
                }));

        return http.build();
    }
}
