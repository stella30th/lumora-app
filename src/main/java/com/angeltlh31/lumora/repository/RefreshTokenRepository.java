package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Dung cho POST /api/users/refresh - token la chuoi PLAIN client gui len, tra cuu truc
    // tiep (khong can Mockito hoa BCrypt.matches nhu password) vi cot token da @Column(unique)
    // -> Postgres tu tao index, tra cuu O(1)/O(log n) thay vi phai quet toan bang.
    Optional<RefreshToken> findByToken(String token);

    // Ngay 15: dung cho logout() - lay het cac token CON HIEU LUC (chua bi revoke) cua 1
    // user de thu hoi toan bo cung luc (dang nhap tu nhieu thiet bi -> logout se dang xuat het).
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
}
