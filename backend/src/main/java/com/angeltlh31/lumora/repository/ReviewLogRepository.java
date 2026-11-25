package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT r.card.id FROM ReviewLog r " +
            "WHERE r.user.id = :userId AND r.card.deck.id = :deckId " +
            "GROUP BY r.card.id HAVING MIN(r.reviewedAt) >= :since")
    List<Long> findCardIdsFirstReviewedSince(@Param("userId") Long userId,
                                             @Param("deckId") Long deckId,
                                             @Param("since") LocalDateTime since);

    long countByUserIdAndReviewedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewLog r WHERE r.card.id = :cardId")
    int deleteByCardId(@Param("cardId") Long cardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ReviewLog r WHERE r.card.id IN (SELECT c.id FROM Card c WHERE c.deck.id = :deckId)")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
