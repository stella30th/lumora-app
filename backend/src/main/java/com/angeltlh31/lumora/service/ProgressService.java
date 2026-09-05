package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.progress.DailyReviewCount;
import com.angeltlh31.lumora.dto.progress.ReviewStatsResponse;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Ngay 18: thong ke o tang "nguoi dung" (cross-deck), khac voi CardProgressService (tang
 * "1 the cu the"). Tach thanh Service rieng thay vi nhet vao CardProgressService vi day la
 * 1 "khach hang" hoan toan khac cua ReviewLog (chi DOC, khong bao gio SUA), va vi
 * CardProgressService da kha day du (submitReview/getDueCards/applyReview) - gop them vao se
 * lam class do "ganh" 2 trach nhiem khac nhau (Single Responsibility Principle).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    // So ngay lich su tra ve mac dinh cho bieu do - 30 ngay ~ 1 thang, du de ve bieu do
    // "theo tuan/thang" nhu frontend Ngay 3 con thieu (xem ghi chu day3-notes.md).
    private static final int HISTORY_DAYS = 30;

    private final ReviewLogRepository reviewLogRepository;

    public ReviewStatsResponse getStats(Long userId) {
        List<LocalDate> reviewDatesDesc = reviewLogRepository.findDistinctReviewDatesByUserId(userId);
        int streak = calculateStreak(reviewDatesDesc);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        long reviewsToday = reviewLogRepository.countByUserIdAndReviewedAtBetween(
                userId, startOfToday, startOfTomorrow);

        List<DailyReviewCount> history = buildHistory(userId, today);

        return ReviewStatsResponse.builder()
                .currentStreakDays(streak)
                .reviewsToday((int) reviewsToday)
                .history(history)
                .build();
    }

    // Ngay 13 tung ap dung cach nay cho applyReview(): bo modifier (package-private) de test
    // cung package goi thang duoc ham nay ma khong can @SpringBootTest/Mockito - day la ham
    // THUAN (khong dung repository/DB), chi nhan vao 1 List<LocalDate> co san va tra ve int,
    // nen test rat nhanh va khong can "gia lap" (mock) gi ca.
    //
    // Quy tac: coi "hom nay" la ngay cuoi cung CAN co mat trong chuoi, TRU KHI hom nay CHUA
    // on the nao ca - luc do lui moc bat dau ve "hom qua" (ngay hom nay van con, chua the coi
    // la "da lam mat chuoi" cho toi khi qua nua dem). Vi du: on lien tuc Thu 2-3-4, hom nay la
    // Thu 5 nhung CHUA on gi -> streak van la 3 (tinh den het Thu 4), khong bi reset ve 0 chi
    // vi chua den luot on hom nay.
    int calculateStreak(List<LocalDate> reviewDatesDesc) {
        if (reviewDatesDesc.isEmpty()) {
            return 0;
        }

        LocalDate cursor = LocalDate.now();
        if (!reviewDatesDesc.get(0).equals(cursor)) {
            cursor = cursor.minusDays(1);
        }

        int streak = 0;
        for (LocalDate reviewDate : reviewDatesDesc) {
            if (reviewDate.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (reviewDate.isBefore(cursor)) {
                // Gap - ngay "cursor" dang cho khong co trong danh sach, nghia la chuoi da
                // dut o day. Danh sach dang xet TU MOI NHAT DEN CU NHAT nen moi ngay con lai
                // phia sau chi cang cu hon - khong con co hoi noi lai chuoi, dung vong lap som.
                break;
            }
            // Truong hop reviewDate.isAfter(cursor) khong xay ra: reviewDatesDesc luon <=
            // LocalDate.now(), va cursor bat dau tu hom nay (hoac hom qua) roi giam dan.
        }
        return streak;
    }

    // Gom nhom du lieu tho tu repository (list "roi rac" theo ngay+dung/sai) thanh danh sach
    // DailyReviewCount LIEN TUC du HISTORY_DAYS phan tu (ke ca ngay khong on gi = 0/0) - xem
    // ly do "khong duoc gay khuc" trong Javadoc cua ReviewStatsResponse.history.
    private List<DailyReviewCount> buildHistory(Long userId, LocalDate today) {
        LocalDate since = today.minusDays(HISTORY_DAYS - 1L);
        LocalDateTime sinceStart = since.atStartOfDay();

        // LinkedHashMap: giu DUNG THU TU chen vao (tu ngay cu nhat den moi nhat, xem vong lap
        // khoi tao ben duoi) - de khi tra ve List (values()), frontend nhan mang da SAP XEP
        // SAN theo ngay tang dan, khong phai tu sap xep lai.
        Map<LocalDate, int[]> countsByDate = new LinkedHashMap<>();
        for (LocalDate date = since; !date.isAfter(today); date = date.plusDays(1)) {
            countsByDate.put(date, new int[]{0, 0}); // [0] = correctCount, [1] = incorrectCount
        }

        List<Object[]> rows = reviewLogRepository.countGroupedByDateAndCorrect(userId, sinceStart);
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            boolean correct = (Boolean) row[1];
            long count = (Long) row[2];
            int[] bucket = countsByDate.get(date);
            if (bucket == null) {
                // Khong the xay ra trong dieu kien binh thuong (query da loc >= sinceStart),
                // nhung bo qua an toan thay vi NullPointerException neu lech mui gio server/DB.
                continue;
            }
            bucket[correct ? 0 : 1] += (int) count;
        }

        return countsByDate.entrySet().stream()
                .map(entry -> DailyReviewCount.builder()
                        .date(entry.getKey())
                        .correctCount(entry.getValue()[0])
                        .incorrectCount(entry.getValue()[1])
                        .build())
                .toList();
    }
}
