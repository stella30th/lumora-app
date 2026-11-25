package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewLog;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({CardService.class, DeckService.class, DeckAccessService.class})
class DeleteCascadeIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private TestEntityManager entityManager;
    @Autowired private CardService cardService;
    @Autowired private DeckService deckService;
    @Autowired private CardRepository cardRepository;
    @Autowired private DeckRepository deckRepository;
    @Autowired private CardProgressRepository cardProgressRepository;
    @Autowired private ReviewLogRepository reviewLogRepository;

    private User owner;
    private Deck deck;

    @BeforeEach
    void setUp() {
        owner = entityManager.persistAndFlush(User.builder()
                .username("owner").email("owner@lumora.test").passwordHash("x").build());
        deck = persistDeck("Deck chinh");
    }

    @Test
    void deleteCard_withProgressAndReviewLog_removesEverything() {
        Card card = persistCard(deck, "apple");
        persistReviewData(card);

        cardService.deleteCard(card.getId(), owner.getId());
        entityManager.flush();

        assertFalse(cardRepository.existsById(card.getId()));
        assertEquals(0, cardProgressRepository.count());
        assertEquals(0, reviewLogRepository.count());
    }

    @Test
    void deleteDeck_withCardsAndReviewData_removesEverythingInThatDeckOnly() {
        Card c1 = persistCard(deck, "apple");
        Card c2 = persistCard(deck, "banana");
        persistReviewData(c1);
        persistReviewData(c2);

        Deck otherDeck = persistDeck("Deck khac");
        Card otherCard = persistCard(otherDeck, "cherry");
        persistReviewData(otherCard);

        deckService.deleteDeck(deck.getId(), owner.getId());
        entityManager.flush();

        assertFalse(deckRepository.existsById(deck.getId()));
        assertFalse(cardRepository.existsById(c1.getId()));
        assertFalse(cardRepository.existsById(c2.getId()));

        assertTrue(deckRepository.existsById(otherDeck.getId()));
        assertTrue(cardRepository.existsById(otherCard.getId()));
        assertEquals(1, cardProgressRepository.count());
        assertEquals(1, reviewLogRepository.count());
    }

    private Deck persistDeck(String name) {
        return entityManager.persistAndFlush(Deck.builder().name(name).owner(owner).build());
    }

    private Card persistCard(Deck d, String term) {
        return entityManager.persistAndFlush(Card.builder().deck(d).term(term).definition("def").build());
    }

    private void persistReviewData(Card card) {
        entityManager.persistAndFlush(CardProgress.builder()
                .user(owner).card(card)
                .status(ReviewStatus.LEARNING)
                .nextReviewDate(LocalDate.now().plusDays(1))
                .build());
        entityManager.persistAndFlush(ReviewLog.builder()
                .user(owner).card(card).correct(true)
                .reviewedAt(LocalDateTime.now())
                .build());
    }
}
