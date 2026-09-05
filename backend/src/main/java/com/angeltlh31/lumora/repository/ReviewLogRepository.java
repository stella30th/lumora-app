package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    @Query("SELECT DISTINCT CAST(r.reviewedAt AS date) FROM ReviewLog r " +
            "WHERE r.user.id = :userId ORDER BY 1 DESC")
    List<LocalDate> findDistinctReviewDatesByUserId(@Param("userId") Long userId);

    @Query("SELECT CAST(r.reviewedAt AS date), r.correct, COUNT(r) FROM ReviewLog r " +
            "WHERE r.user.id = :userId AND r.reviewedAt >= :since " +
            "GROUP BY CAST(r.reviewedAt AS date), r.correct")
    List<Object[]> countGroupedByDateAndCorrect(@Param("userId") Long userId,
                                                 @Param("since") LocalDateTime since);

    long countByUserIdAndReviewedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
