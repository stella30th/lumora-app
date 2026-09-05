package com.angeltlh31.lumora.dto.deck;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeckResponse {

    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private Long ownerId;
    private LocalDateTime createdAt;
}
