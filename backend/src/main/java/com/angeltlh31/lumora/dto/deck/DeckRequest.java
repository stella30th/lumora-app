package com.angeltlh31.lumora.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeckRequest {

    @NotBlank(message = "Deck name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private boolean isPublic;
}
