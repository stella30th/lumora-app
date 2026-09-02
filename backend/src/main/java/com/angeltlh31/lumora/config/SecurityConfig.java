package com.angeltlh31.lumora.config;

import com.angeltlh31.lumora.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring tu tim va inject bean JwtAuthenticationFilter (da danh dau @Component) vao day -
    // khong can khai bao @Bean rieng cho no, chi can no ton tai trong Spring context.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Ngay 17 - CORS: danh sach origin (scheme+host+port cua TRINH DUYET, khong phai server)
    // duoc phep goi API nay bang JavaScript. Doc tu property, KHONG hardcode trong code - vi
    // gia tri nay CHAC CHAN khac nhau giua dev (frontend chay localhost:3000) va production
    // (frontend that tren Vercel, vd https://lumora.vercel.app) - giong tinh than jwt.secret
    // o application.properties (default cho dev, ghi de bang bien moi truong luc deploy that).
    // Nhieu origin cach nhau boi dau phay (","), xem CorsConfig ben duoi.
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    // Bean dung chung cho ca app: hash password luc register, so khop luc login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Ngay 17 - CORS: cau hinh CHI TIET "ai duoc phep goi, goi bang cach nao" - tach rieng
    // thanh 1 bean de securityFilterChain() ben duoi chi can tham chieu toi, khong nhoi het
    // logic vao 1 cho. Xem giai thich tung dong trong docs/recap-day17.md.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // setAllowedOrigins (KHONG phai setAllowedOriginPatterns hay "*"): liet ke CHINH XAC
        // tung origin duoc phep, khong dung wildcard "*" - vi "*" + gui kem Authorization
        // header bi trinh duyet/spec CORS coi la khong an toan trong nhieu tinh huong, va quan
        // trong hon: wildcard nghia la BAT KY website nao tren Internet cung goi duoc API nay
        // tu trinh duyet nguoi dung khac - mat het y nghia "chi frontend cua toi moi duoc goi".
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Cac phuong thuc HTTP frontend se dung - OPTIONS bat buoc phai co du day la method
        // dung rieng cho preflight request (xem docs/recap-day17.md muc 3), khong phai method
        // nghiep vu nao ca.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Header frontend duoc phep tu dinh kem trong request - PHAI co "Authorization" (JWT
        // access token gui kem moi request) va "Content-Type" (frontend gui JSON body).
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // allowCredentials KHONG bat (mac dinh false) - chi can bat = true khi frontend dung
        // COOKIE (trinh duyet tu dong dinh kem) de xac thuc. Du an nay gui JWT qua header
        // Authorization (frontend tu tay doc token va tu gan vao header, KHONG phai cookie
        // trinh duyet tu dong gui) nen khong can - xem docs/recap-day17.md muc 5.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // "/**" - ap dung cau hinh nay cho MOI route cua app, khong rieng gi API - don gian
        // hoa cho du an quy mo nay (chi co 1 nhom API duy nhat, khong can phan biet CORS khac
        // nhau giua cac nhom route).
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // QUAN TRONG: chi co starter-security tren classpath la Spring da tu dong khoa
    // TOAN BO endpoint bang HTTP Basic Auth mac dinh (sinh 1 password ngau nhien in ra console).
    // Filter chain ben duoi ghi de lai hanh vi mac dinh do.
    //
    // Buoc 5 (JWT Filter) sua lai placeholder cua ngay 5: gio moi request bat buoc phai co
    // token hop le, TRU cac endpoint permitAll ben duoi.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Ngay 17: kich hoat CORS, tro toi bean corsConfigurationSource() o tren. Dat o
                // dong DAU TIEN co chu dich (du thu tu goi cac .xxx() ben duoi khong lam thay
                // doi ket qua - Spring Security tu sap xep filter that su theo loai, khong theo
                // thu tu code) - de nguoi doc thay CORS la buoc dau tien can nghi toi khi debug
                // loi tu frontend.
                //
                // Vi sao khong can tu them "/**".permitAll() rieng cho method OPTIONS (preflight):
                // Spring Security xu ly CORS o 1 CorsFilter rieng, chay TRUOC ca buoc xac thuc/
                // phan quyen (truoc JwtAuthenticationFilter lan .authorizeHttpRequests() ben
                // duoi) - preflight request (OPTIONS) duoc tra loi va dung lai luon o day, khong
                // bao gio di toi buoc doi hoi token ca. Xem chi tiet trong recap-day17.md.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF la co che bao ve session/cookie cua trinh duyet - REST API dung token
                // (khong dung cookie de xac thuc) nen khong can, tat di cho don gian.
                .csrf(csrf -> csrf.disable())
                // STATELESS: bao Spring KHONG tao/luu HttpSession cho user.
                // Dung tinh than cua JWT - moi request tu mang du thong tin xac thuc trong token,
                // server khong nho "ai vua dang nhap" giua cac request.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sieu lai placeholder permitAll() cua ngay 5: chi cac endpoint nay khong can
                // access token - vi chinh no la noi TAO/DOI moi ra token, hoac la Swagger UI.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                // Ngay 15: /refresh KHONG the doi hoi access token hop le - endpoint
                                // nay ton tai CHINH VI access token da het han. Xac thuc o day dua
                                // vao refreshToken trong body (RefreshTokenService), khong dua vao
                                // JwtAuthenticationFilter/SecurityFilterChain nhu cac route khac.
                                "/api/users/refresh",
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
