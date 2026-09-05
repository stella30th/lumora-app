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

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ReviewLogRepository reviewLogRepository;

    @InjectMocks
    private ProgressService service;

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

        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(
                today.minusDays(1), today.minusDays(2));

        assertEquals(2, service.calculateStreak(datesDesc));
    }

    @Test
    void calculateStreak_gapInTheMiddle_stopsCountingAtTheGap() {

        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(
                today, today.minusDays(1), today.minusDays(4), today.minusDays(5));

        assertEquals(2, service.calculateStreak(datesDesc));
    }

    @Test
    void calculateStreak_missedTodayAndYesterday_isZero() {

        LocalDate today = LocalDate.now();
        List<LocalDate> datesDesc = List.of(today.minusDays(3), today.minusDays(4));

        assertEquals(0, service.calculateStreak(datesDesc));
    }

    @Test
    void getStats_combinesStreakTodayCountAndHistoryIntoOneResponse() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        when(reviewLogRepository.findDistinctReviewDatesByUserId(userId))
                .thenReturn(List.of(today, today.minusDays(1)));
        when(reviewLogRepository.countByUserIdAndReviewedAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Object[] row = new Object[]{today, Boolean.TRUE, 3L};

        when(reviewLogRepository.countGroupedByDateAndCorrect(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(row));

        ReviewStatsResponse stats = service.getStats(userId);

        assertEquals(2, stats.getCurrentStreakDays());
        assertEquals(5, stats.getReviewsToday());

        assertEquals(30, stats.getHistory().size());
        int todayCorrectCount = stats.getHistory().stream()
                .filter(h -> h.getDate().equals(today))
                .findFirst().orElseThrow()
                .getCorrectCount();
        assertEquals(3, todayCorrectCount);
    }
}
