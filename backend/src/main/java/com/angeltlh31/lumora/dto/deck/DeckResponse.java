package com.angeltlh31.lumora.dto.deck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonIgnoreProperties("public")
public class DeckResponse {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private Long ownerId;
    private LocalDateTime createdAt;
}
