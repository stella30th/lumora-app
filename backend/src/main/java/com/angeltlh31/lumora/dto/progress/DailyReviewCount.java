package com.angeltlh31.lumora.dto.progress;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// 1 diem du lieu tren bieu do lich su (truc X = date). correctCount/incorrectCount tach rieng
// (khong gop chung "totalCount") de frontend ve duoc bieu do stacked/ty le dung/sai theo ngay
// neu muon, thay vi chi 1 con so tong.
@Getter
@Builder
public class DailyReviewCount {
    private LocalDate date;
    private int correctCount;
    private int incorrectCount;
}
