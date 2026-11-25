package com.angeltlh31.lumora.dto.cardprogress;

import com.angeltlh31.lumora.entity.ReviewRating;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardReviewRequest {
    @NotNull(message = "rating is required (AGAIN, HARD, GOOD or EASY)")
    private ReviewRating rating;
}
