package com.angeltlh31.lumora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Bean dung chung cho ca app: hash password luc register, so khop luc login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // QUAN TRONG: chi co starter-security tren classpath la Spring da tu dong khoa
    // TOAN BO endpoint bang HTTP Basic Auth mac dinh (sinh 1 password ngau nhien in ra console).
    // Filter chain ben duoi ghi de lai hanh vi mac dinh do:
    // - permitAll() tam thoi cho MOI request, vi chua co JWT filter de xac thuc that.
    // - se sua lai o bai JWT: chi permitAll cho /api/users/register va /api/users/login,
    //   con lai bat buoc phai co token hop le.
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
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
