package com.angeltlh31.lumora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Ngay 15 - luu 1 "phien dang nhap dai han" cho 1 User. Khac han access token (JWT o
 * JwtService) - access token la STATELESS (server khong luu gi ca, chi verify chu ky), con
 * RefreshToken la STATEFUL: moi dong trong bang nay la 1 dong CO THAT trong Postgres, nen
 * server co the "thu hoi" (revoke) no bat cu luc nao (logout, phat hien bi lo...) - dieu ma
 * JWT thuan tuy KHONG lam duoc (JWT van con "hop le" theo chu ky cho toi khi het han tu nhien,
 * du server co muon huy no som cach may cung khong duoc, vi server khong luu gi ca).
 * Xem docs/recap-day15.md phan ly thuyet de hieu vi sao can ca 2 loai token cung luc.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Chuoi ngau nhien entropy cao (xem RefreshTokenService.generateSecureToken) - dong vai
    // tro "chia khoa" client giu de doi lay access token moi. LUU DANG PLAIN TEXT (khong hash
    // nhu password) de tra cuu O(1) qua findByToken() luc goi /refresh - day la mot danh doi
    // BAO MAT CO Y THUC, khong phai thieu sot: giai thich day du trong RefreshTokenService va
    // docs/recap-day15.md (muc "Vi sao KHONG hash RefreshToken nhu password").
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // true = token nay khong con dung de refresh duoc nua (da bi "dot" 1 lan qua /refresh -
    // xem "token rotation" trong RefreshTokenService -, hoac user da logout). Van GIU LAI dong
    // nay trong DB thay vi xoa ngay - phuc vu audit ("ai dang nhap tu dau, luc nao") va phat
    // hien bat thuong (1 refresh token bi dung LAI sau khi da revoked la dau hieu no da bi
    // danh cap - xem cau hoi tu kiem tra so 4 trong docs/recap-day15.md).
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
