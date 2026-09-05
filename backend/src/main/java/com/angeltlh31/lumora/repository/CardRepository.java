package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    @Query("""
            SELECT c FROM Card c
            LEFT JOIN CardProgress cp ON cp.card = c AND cp.user.id = :userId
            WHERE c.deck.id = :deckId
              AND (cp IS NULL OR cp.nextReviewDate <= :today)
            """)
    List<Card> findDueCards(@Param("deckId") Long deckId,
                             @Param("userId") Long userId,
                             @Param("today") LocalDate today);
}
