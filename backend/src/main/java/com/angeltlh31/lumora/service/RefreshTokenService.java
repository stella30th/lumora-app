package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.InvalidRefreshTokenException;
import com.angeltlh31.lumora.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/*
 * Ngay 15 - noi duy nhat "biet" ve vong doi cua 1 RefreshToken: tao moi, kiem tra con hop le
 * hay khong, va thu hoi (revoke). UserService (buoc goi) khong tu tay dung SecureRandom hay
 * tu so sanh expiresAt - giao het cho Service nay, giong tinh than JwtService da tach rieng
 * "biet ve JWT" ra khoi UserService tu Ngay 6.
 *
 * Vi sao KHONG hash RefreshToken nhu password (BCryptPasswordEncoder da co san):
 * - Password la chuoi NGUOI DUNG TU NGHI (entropy thap - "123456", ten con vat...), nen PHAI
 *   hash bang thuat toan CHAM CO CHU DICH (BCrypt) de ke tan cong khong the "do het" hang ty
 *   kha nang trong vai giay du co lo ca file DB.
 * - RefreshToken la chuoi MAY TU SINH bang SecureRandom, 256 bit ngau nhien tuyet doi (xem
 *   generateSecureToken() ben duoi) - khong ai "doan" ra duoc du co hash hay khong, vi khong
 *   co "tu dien" nao de do ca. Cai can o day la TRA CUU NHANH (findByToken chay moi request
 *   goi /refresh), ma BCrypt.matches() thi KHONG THE tra cuu truc tiep bang cot da hash duoc
 *   (moi lan hash cung 1 chuoi ra ket qua KHAC nhau do salt ngau nhien - xem lai
 *   docs/recap-day5.md) - se phai quet toan bo bang roi so tung dong, cham dan theo so user.
 * - Danh doi thuc su: neu ke tan cong doc duoc TOAN BO Postgres (vd loi SQL injection khac),
 *   ho co the dung THANG cac token con hieu luc trong bang nay. Day la ly do cac he thong lon
 *   (OAuth2 provider...) dung mo hinh "selector + validator" (2 nua: 1 nua tra cuu duoc, 1
 *   nua hash) de vua nhanh vua an toan khi lo DB - qua pham vi 1 buoi hoc, ghi lai o day de
 *   ban tu tim hieu them khi can nang cap.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // SecureRandom (KHAC java.util.Random binh thuong): dung nguon entropy that cua he dieu
    // hanh (/dev/urandom tren Linux), khong the doan truoc duoc chuoi so tiep theo du biet
    // toan bo lich su cac so da sinh ra - bat buoc cho moi thu lien quan toi bao mat (khoa,
    // token, salt...). java.util.Random CHI la bo sinh so gia-ngau-nhien (PRNG) du doan duoc
    // neu biet seed, TUYET DOI khong dung cho bao mat.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    // readOnly = true vi chi doc - nhung method GOI ham nay (UserService.refreshAccessToken)
    // se tu mo transaction GHI rieng cho buoc revoke + tao token moi, xem giai thich o do.
    @Transactional(readOnly = true)
    public RefreshToken verifyAndGet(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // Dau hieu dang ngo: 1 token DA bi revoke (da dung de refresh 1 lan, hoac da
            // logout) ma van co ai do dem di dung lai - trong he thong that, day la luc nen
            // canh bao/ghi log rieng (nghi ngo token bi danh cap) - xem cau hoi tu kiem tra 4.
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked - please login again");
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has expired - please login again");
        }
        return refreshToken;
    }

    public void revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // Logout: thu hoi TAT CA token con hieu luc cua user nay (don gian hoa - dang xuat khoi
    // moi thiet bi cung luc, khong phan biet "thiet bi nao dang goi logout"). Muon logout
    // RIENG 1 thiet bi thi endpoint /logout can nhan them chinh refresh token cua thiet bi do
    // trong body, roi chi revoke() dung 1 dong - de danh cho buoi sau neu can.
    public void revokeAllForUser(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    // Base64 URL-safe (khong dung Base64 thuong): token nay se di qua URL/JSON/header trong
    // request - bang Base64 thuong co ky tu "+" va "/" phai encode lai moi khi dat vao URL,
    // bang URL-safe thay 2 ky tu do bang "-" va "_" de dung thang duoc khong can encode them.
    // withoutPadding(): bo dau "=" o cuoi (dem so byte da du, khong can padding de biet do dai).
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32]; // 32 byte = 256 bit ngau nhien - cung muc voi khoa JWT
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
