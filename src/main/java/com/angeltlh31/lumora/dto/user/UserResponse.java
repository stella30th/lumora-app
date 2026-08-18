package com.angeltlh31.lumora.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Chu y: KHONG co field password/passwordHash o day - khong bao gio tra mat khau ve cho client.
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
