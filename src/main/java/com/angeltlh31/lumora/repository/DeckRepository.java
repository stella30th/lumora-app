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


}
