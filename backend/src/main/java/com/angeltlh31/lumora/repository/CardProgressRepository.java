package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {
    Optional<CardProgress> findByUserIdAndCardId(Long userId, Long cardId);

    List<CardProgress> findByUserIdAndCardIdIn(Long userId, List<Long> cardIds);

    @Query("SELECT cp.card.deck.id, COUNT(cp), SUM(CASE WHEN cp.nextReviewDate <= :today THEN 1 ELSE 0 END) " +
            "FROM CardProgress cp WHERE cp.user.id = :userId AND cp.card.deck.owner.id = :userId " +
            "GROUP BY cp.card.deck.id")
    List<Object[]> progressStatsPerOwnDeck(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CardProgress cp WHERE cp.card.id = :cardId")
    int deleteByCardId(@Param("cardId") Long cardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CardProgress cp WHERE cp.card.id IN (SELECT c.id FROM Card c WHERE c.deck.id = :deckId)")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
