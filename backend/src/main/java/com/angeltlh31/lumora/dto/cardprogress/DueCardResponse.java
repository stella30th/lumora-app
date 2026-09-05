package com.angeltlh31.lumora.dto.cardprogress;

import com.angeltlh31.lumora.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DueCardResponse {
    private Long id;
    private String term;
    private String definition;
    private Long deckId;
    private ReviewStatus status;
    private LocalDate nextReviewDate;
}
