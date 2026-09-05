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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

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

                break;
            }

        }
        return streak;
    }

    private List<DailyReviewCount> buildHistory(Long userId, LocalDate today) {
        LocalDate since = today.minusDays(HISTORY_DAYS - 1L);
        LocalDateTime sinceStart = since.atStartOfDay();

        Map<LocalDate, int[]> countsByDate = new LinkedHashMap<>();
        for (LocalDate date = since; !date.isAfter(today); date = date.plusDays(1)) {
            countsByDate.put(date, new int[]{0, 0});
        }

        List<Object[]> rows = reviewLogRepository.countGroupedByDateAndCorrect(userId, sinceStart);
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            boolean correct = (Boolean) row[1];
            long count = (Long) row[2];
            int[] bucket = countsByDate.get(date);
            if (bucket == null) {

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
