package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.progress.ReviewStatsResponse;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/*
 * Cung 2 tang test nhu CardProgressServiceTest (xem file do de doi chieu ly thuyet Mockito
 * chi tiet hon): calculateStreak() la logic THUAN (khong dung DB) nen test truc tiep khong
 * can mock; getStats() co goi ra ngoai (ReviewLogRepository) nen can @Mock/@InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ReviewLogRepository reviewLogRepository;

    @InjectMocks
    private ProgressService service;

    // ============================================================
    // calculateStreak() - logic thuan, tu tao du lieu dau vao, khong dung mock
    // ============================================================

    @Test
    void calculateStreak_noReviewsAtAll_isZero() {
        assertEquals(0, service.calculateStreak(Collections.emptyList()));
    }

    @Test
    void calculateStreak_reviewedTodayAndConsecutiveDaysBefore_countsAll() {
        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(
                today, today.minusDays(1), today.minusDays(2));

        assertEquals(3, service.calculateStreak(datesDesc));
    }

    @Test
    void calculateStreak_notReviewedTodayButReviewedYesterday_stillCountsUpToYesterday() {
        // Chua on gi hom nay, nhung hom qua/hom-kia co on lien tuc - streak KHONG bi reset ve
        // 0 chi vi "chua den luot hom nay" (xem Javadoc calculateStreak trong ProgressService).
        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(
                today.minusDays(1), today.minusDays(2));

        assertEquals(2, service.calculateStreak(datesDesc));
    }

    @Test
    void calculateStreak_gapInTheMiddle_stopsCountingAtTheGap() {
        // On hom nay va hom qua, NHUNG bo lo 2 ngay truoc do (hom-kia), roi lai co on xa hon
        // nua - phan xa hon (sau khoang trong) khong duoc tinh vao streak hien tai.
        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(
                today, today.minusDays(1), today.minusDays(4), today.minusDays(5));

        assertEquals(2, service.calculateStreak(datesDesc));
    }

    @Test
    void calculateStreak_missedTodayAndYesterday_isZero() {
        // Lan on gan nhat la 3 ngay truoc - ca hom nay lan hom qua deu KHONG co, chuoi coi
        // nhu da mat (khac voi truong hop "chi moi bo lo hom nay" o test tren).
        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(today.minusDays(3), today.minusDays(4));

        assertEquals(0, service.calculateStreak(datesDesc));
    }

    // ============================================================
    // getStats() - co goi ra ngoai (ReviewLogRepository) -> phai stub bang when(...)
    // ============================================================

    @Test
    void getStats_combinesStreakTodayCountAndHistoryIntoOneResponse() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        when(reviewLogRepository.findDistinctReviewDatesByUserId(userId))
                .thenReturn(List.of(today, today.minusDays(1)));
        when(reviewLogRepository.countByUserIdAndReviewedAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        // 1 dong du lieu tho: hom nay co 3 lan DUNG - phan con lai cua 30 ngay se duoc
        // buildHistory() tu dien day 0/0 (xem ProgressService.buildHistory()).
        Object[] row = new Object[]{today, Boolean.TRUE, 3L};
        // Collections.singletonList(row) - KHONG dung List.of(row): List.of(E... elements)
        // la varargs, va row DA CO SAN kieu Object[] nen javac hieu nham thanh "list 3
        // phan tu Object" (unpack row ra) thay vi "list 1 phan tu la chinh row" - loi bien
        // dich that su gap phai, xem lai neu quen. singletonList(T o) nhan 1 tham so thuong
        // (khong phai varargs) nen khong bi nham nhu vay.
        when(reviewLogRepository.countGroupedByDateAndCorrect(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(row));

        ReviewStatsResponse stats = service.getStats(userId);

        assertEquals(2, stats.getCurrentStreakDays());
        assertEquals(5, stats.getReviewsToday());
        // 30 ngay lien tuc (HISTORY_DAYS trong ProgressService), KHONG bi thieu ngay nao du
        // repository chi tra ve dung 1 dong du lieu tho cho 1 ngay.
        assertEquals(30, stats.getHistory().size());
        int todayCorrectCount = stats.getHistory().stream()
                .filter(h -> h.getDate().equals(today))
                .findFirst().orElseThrow()
                .getCorrectCount();
        assertEquals(3, todayCorrectCount);
    }
}
