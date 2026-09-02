package com.angeltlh31.lumora.dto.cardprogress;

import com.angeltlh31.lumora.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CardProgressResponse {
    private Long id;
    private Long cardId;
    private Double easeFactor;
    private Integer intervalDays;
    private Integer repetitions;
    private ReviewStatus status;
    private LocalDate nextReviewDate;
    private LocalDateTime lastReviewedAt;
}
