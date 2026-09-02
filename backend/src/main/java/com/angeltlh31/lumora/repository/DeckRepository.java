package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByOwnerId (Long ownerId);
    //Derived Query Methods
        //phải có findBy -> báo cho Spring đây là 1 query
        //findBy + OwnerId là ket họp cua owner(deck) và id(user)
        // -> SELECT * FROM decks WHERE owner_id = ?

    // Ngay 11: dung cho route "kham pha" Deck cong khai - KHONG loc theo owner, chi loc theo
    // cot is_public. Ten method phai khop dung ten field Java "isPublic" + hau to "True" cho
    // kieu boolean de Spring Data JPA tu sinh dung query:
    // -> SELECT * FROM decks WHERE is_public = true
    List<Deck> findByIsPublicTrue();

}
