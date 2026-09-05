package com.angeltlh31.lumora.dto.progress;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyReviewCount {
    private LocalDate date;
    private int correctCount;
    private int incorrectCount;
}
