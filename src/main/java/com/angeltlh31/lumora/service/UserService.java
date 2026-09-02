package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
import com.angeltlh31.lumora.entity.RefreshToken;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.DuplicateResourceException;
import com.angeltlh31.lumora.exception.InvalidCredentialException;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.UserRepository;
import com.angeltlh31.lumora.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // Ngay 15: them dependency thu 4 - moi logic ve RefreshToken (tao/kiem tra/thu hoi) giao
    // het cho Service rieng nay, UserService chi GOI, khong tu tay dung SecureRandom hay tu
    // so sanh expiresAt (xem RefreshTokenService de biet vi sao tach rieng).
    private final RefreshTokenService refreshTokenService;

    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username da ton tai: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email da ton tai: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                // Hash bang BCrypt - moi lan encode() cung 1 input se ra output KHAC nhau (co salt ngau nhien
                // gan trong chinh chuoi hash), nen khong the "giai ma" nguoc lai, chi co the SO KHOP
                // bang passwordEncoder.matches(rawPassword, hash) luc Login.
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        return toResponse(userRepository.save(user));
    }

    // Ngay 15: KHONG con readOnly = true nua - login() gio CO GHI xuong DB (tao 1 dong
    // RefreshToken moi qua refreshTokenService.createRefreshToken()), khac voi truoc day
    // (Ngay 6) chi doc User roi generateToken() thuan tuy trong bo nho, khong dung gi toi DB.
    public LoginResponse login(LoginRequest request) {
        // Co tinh dung CUNG 1 message cho ca 2 nhanh loi (xem InvalidCredentialException).
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Email hoac password khong dung"));

        // matches(raw, hash): hash lai raw roi so voi hash da luu - KHONG giai ma hash de so sanh truc tiep
        // (khong the, BCrypt la ham bam 1 chieu - xem docs/recap-day5.md).
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialException("Email hoac password khong dung");
        }

        return issueTokenPair(user);
    }

    // Ngay 15 - trai tim cua tinh nang refresh: doi 1 refresh token CON HOP LE lay 1 CAP
    // token MOI hoan toan (ca access LAN refresh, khong chi rieng access token).
    //
    // "Token rotation" (xoay vong token): refresh token CU bi thu hoi (revoke) NGAY SAU KHI
    // dung, du no chua het han - khong cho dung lai lan 2. Vi sao lam vay thay vi giu nguyen
    // refresh token cu, chi cap access token moi? Neu 1 refresh token bi ke tan cong danh cap
    // (vd lo tu localStorage qua XSS) VA CA nguoi dung that lan ke tan cong cung dung no de
    // refresh, thi nguoi den SAU se bi tu choi (token da bi revoke boi nguoi den TRUOC) - day
    // la tin hieu ro rang "co gi do bat thuong dang xay ra" ma mo hinh "refresh token song
    // mai khong doi" khong the phat hien duoc.
    public LoginResponse refreshAccessToken(String rawRefreshToken) {
        RefreshToken oldToken = refreshTokenService.verifyAndGet(rawRefreshToken);
        User user = oldToken.getUser();

        // Thu hoi token cu TRUOC KHI tao token moi (thu tu quan trong - neu tao truoc, chua
        // kip revoke ma request bi loi giua chung, token cu van con "song" gay kho hieu khi debug).
        refreshTokenService.revoke(oldToken);

        return issueTokenPair(user);
    }

    // Ngay 15: logout - thu hoi TOAN BO refresh token con hieu luc cua user. Luu y: access
    // token (JWT) HIEN TAI cua nguoi goi VAN CON hop le cho toi khi tu het han (JWT la
    // stateless, server khong "xoa" duoc no som hon) - logout o day chi dam bao KHONG THE xin
    // access token MOI qua /refresh nua, chu khong "vo hieu hoa ngay lap tuc" access token dang cam.
    // Day la gioi han von co cua JWT thuan, khong phai loi cua code - muon logout tuc thi hoan
    // toan can them 1 "blacklist" access token rieng (danh cho buoi nang cao sau).
    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay User id=" + id));
        return toResponse(user);
    }

    // Dung chung cho ca login() lan refreshAccessToken() - ca 2 tinh huong deu ket thuc bang
    // "phat 1 cap token moi cho dung User nay", chi khac o BUOC XAC THUC truoc do (password
    // vs refresh token cu). Gom lai tranh lap code sinh access token + tao RefreshToken 2 lan.
    private LoginResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
