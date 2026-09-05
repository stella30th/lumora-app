package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByOwnerId (Long ownerId);

    List<Deck> findByIsPublicTrue();

}
