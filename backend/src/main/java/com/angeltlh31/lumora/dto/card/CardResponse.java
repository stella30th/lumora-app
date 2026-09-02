package com.angeltlh31.lumora.dto.card;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardResponse {

    private Long id;
    private String term;
    private String definition;
    private Long deckId;
    private LocalDateTime createdAt;
}
