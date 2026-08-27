package com.angeltlh31.lumora.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// Day la "middleware" that su cua Spring Security: extends OncePerRequestFilter nghia la
// class nay chen vao GIUA luc request toi server va luc no cham duoc Controller - dung 1
// LAN cho moi request (khac GlobalExceptionHandler chi chay SAU khi exception da xay ra).
//
// Nhiem vu duy nhat: doc header "Authorization: Bearer <token>", neu token hop le thi
// "dang nhap ho" request nay bang cach ghi vao SecurityContextHolder - noi ma ca
// SecurityFilterChain (buoc sau, quyet dinh permitAll/authenticated) lan Controller
// (qua @AuthenticationPrincipal) deu doc lai duoc.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        // Khong co header, hoac sai dinh dang "Bearer <token>" -> khong co gi de xac thuc,
        // cho request di tiep VOI SecurityContext rong. Day KHONG phai loi: mot request toi
        // /api/users/login (permitAll) van chay qua day, chi la khong ai duoc "dang nhap ho".
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Long userId = jwtService.extractUserId(token);

            // UsernamePasswordAuthenticationToken la 1 "vo boc" chuan cua Spring Security cho
            // biet "request nay da duoc xac thuc, va day la principal (danh tinh) cua no".
            // - principal = userId: se lay lai duoc trong Controller qua @AuthenticationPrincipal Long.
            // - credentials = null: khong con can password/token nua, viec xac thuc da xong roi.
            // - authorities = rong: chua lam phan quyen (Authorization) - de danh cho buoi sau.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Ghi vao SecurityContextHolder = "danh dau request nay la cua userId nay" cho
            // toan bo phan con lai cua request (SecurityFilterChain + Controller) doc duoc.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            // Token sai chu ky / het han / sai dinh dang -> KHONG set Authentication.
            // Co tinh nuot loi o day thay vi tra ve 401 ngay: de SecurityFilterChain (buoc sau)
            // la noi DUY NHAT quyet dinh endpoint nao can dang nhap. Neu endpoint dang goi la
            // permitAll (vd /login) thi request van di tiep binh thuong du token co sai.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
