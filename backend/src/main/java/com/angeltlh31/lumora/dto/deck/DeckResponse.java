package com.angeltlh31.lumora.dto.deck;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Du lieu tra VE cho client. Chi chua nhung gi client can thay.
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
