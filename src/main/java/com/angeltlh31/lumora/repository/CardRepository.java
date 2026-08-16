package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    // Giong findByOwnerId ben DeckRepository: -> SELECT * FROM cards WHERE deck_id = ?
    List<Card> findByDeckId(Long deckId);

}
