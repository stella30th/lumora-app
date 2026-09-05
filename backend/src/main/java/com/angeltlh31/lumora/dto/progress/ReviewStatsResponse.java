package com.angeltlh31.lumora.dto.progress;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewStatsResponse {
    // So ngay lien tuc (tinh ca hom nay hoac hom qua neu hom nay chua on) nguoi dung on it
    // nhat 1 the - xem ProgressService.calculateStreak() de biet chi tiet quy tac.
    private int currentStreakDays;
    private int reviewsToday;
    // Lich su N ngay gan nhat (mac dinh 30, xem ProgressService.HISTORY_DAYS) - LUON du
    // dung N ngay lien tuc ke ca ngay khong on the nao (correctCount=incorrectCount=0), de
    // frontend ve truc X lien tuc, khong bi "gay khuc" o ngay thieu du lieu.
    private List<DailyReviewCount> history;
}
