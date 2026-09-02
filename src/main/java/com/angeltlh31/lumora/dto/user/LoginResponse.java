package com.angeltlh31.lumora.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    // Access token (JWT) - dinh kem vao header "Authorization: Bearer <token>" cho MOI request
    // toi API can dang nhap. Song NGAN (xem jwt.expiration-ms - Ngay 15 rut tu 24h xuong 15
    // phut) - vi day la thu duy nhat 1 ke tan cong can co de gia mao request, cang song ngan
    // cang giam thiet hai neu bi lo.
    private String token;

    // Ngay 15: refresh token - CHI dung 1 lan duy nhat, GUI DUY NHAT toi 1 endpoint duy nhat
    // (POST /api/users/refresh) de doi lay 1 cap token moi (access + refresh) khi access token
    // het han - khong dinh kem vao cac request API thong thuong nhu token o tren. Song LAU hon
    // han (7 ngay) vi it bi lo hon nhieu (chi truyen dung 1 lan luc login/refresh, khong bay
    // theo MOI request nhu access token).
    private String refreshToken;

    private Long userId;
    private String username;
}
