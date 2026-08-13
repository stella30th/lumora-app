package com.angeltlh31.lumora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
// "user" la tu khoa dung trong Postgres (vd: CREATE USER), nen dat ten bang khac de tranh xung dot
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//Cung cấp mẫu thiết kế Builder Pattern, giúp mày tạo một đối tượng User mới cực kỳ ngắn gọn kiểu User.builder().username("abc").build() thay vì dùng new User().
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Chi luu password da hash (BCrypt) - khong bao gio luu plain text.
    // Se dung Spring Security PasswordEncoder de hash khi hoc toi phan Security.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
