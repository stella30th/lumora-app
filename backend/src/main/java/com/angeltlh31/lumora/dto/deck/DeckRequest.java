package com.angeltlh31.lumora.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Du lieu client gui LEN khi tao/sua Deck. Khong dung Entity Deck truc tiep lam input.
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
