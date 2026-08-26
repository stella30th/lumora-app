package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.user.LoginRequest;
import com.angeltlh31.lumora.dto.user.LoginResponse;
import com.angeltlh31.lumora.dto.user.UserRegisterRequest;
import com.angeltlh31.lumora.dto.user.UserResponse;
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

    // readOnly = true: login chi doc DB (khong ghi), Hibernate bo qua buoc kiem tra dirty checking
    // luc commit transaction - nhanh hon mot chut va noi ro y dinh cho nguoi doc code.
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Co tinh dung CUNG 1 message cho ca 2 nhanh loi (xem InvalidCredentialException).
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialException("Email hoac password khong dung"));

        // matches(raw, hash): hash lai raw roi so voi hash da luu - KHONG giai ma hash de so sanh truc tiep
        // (khong the, BCrypt la ham bam 1 chieu - xem docs/recap-day5.md).
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialException("Email hoac password khong dung");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay User id=" + id));
        return toResponse(user);
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
