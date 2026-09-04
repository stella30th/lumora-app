package com.angeltlh31.lumora.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRequest {

    @NotBlank(message = "Term is required")
    @Size(max = 255)
    private String term;

    @NotBlank(message = "Definition is required")
    @Size(max = 1000)
    private String definition;
}
