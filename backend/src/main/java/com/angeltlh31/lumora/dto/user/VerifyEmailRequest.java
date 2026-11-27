package com.angeltlh31.lumora.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {
    @NotBlank(message = "Token is required")
    @Size(max = 100)
    private String token;
}
