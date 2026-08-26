package com.angeltlh31.lumora.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
}
