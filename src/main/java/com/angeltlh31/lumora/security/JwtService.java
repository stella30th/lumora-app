package com.angeltlh31.lumora.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Noi duy nhat trong app biet ve "cach tao/doc JWT". UserService chi goi generateToken(),
// khong can biet chi tiet thuat toan ky hay claim nam o dau - tach rieng de sau nay JwtFilter
// (buoc 5, doc token) cung dung lai duoc chinh Service nay de verify, khong viet code trung lap.
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // HS256 yeu cau khoa ky >= 256 bit (32 byte). Keys.hmacShaKeyFor tu kiem tra dieu nay -
    // secret qua ngan se nem loi ngay luc start app, tot hon la loi ngam luc verify token sau nay.
    private SecretKey getSigningKey() {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Payload (claims) chi nen chua thong tin KHONG nhay cam va can de xac dinh danh tinh:
    // userId lam "subject" (chuan JWT), username them cho tien doc log/debug.
    // TUYET DOI khong nhet password hay du lieu nhay cam - payload chi encode Base64, ai cung doc duoc.
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }
}
