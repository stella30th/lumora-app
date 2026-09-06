package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    Page<Deck> findByOwnerId(Long ownerId, Pageable pageable);

    List<Deck> findByIsPublicTrue();

}
