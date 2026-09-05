package com.angeltlh31.lumora.dto.progress;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewStatsResponse {

    private int currentStreakDays;
    private int reviewsToday;

    private List<DailyReviewCount> history;
}
