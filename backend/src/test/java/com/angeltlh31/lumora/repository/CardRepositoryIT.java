package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

@Testcontainers
class CardRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    private User owner;
    private Deck deck;

    @BeforeEach
    void setUp() {
        owner = persistUser("owner", "owner@lumora.test");
        deck = persistDeck(owner, "Tieng Anh co ban");
    }

    @Test
    void findDueCards_includesCardWithNoProgressYet() {
        Card card = persistCard(deck, "apple", "qua tao");

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    @Test
    void findDueCards_includesCardWhoseNextReviewDateIsTodayOrEarlier() {
        Card card = persistCard(deck, "apple", "qua tao");
        persistProgress(owner, card, LocalDate.now().minusDays(1));

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    @Test
    void findDueCards_excludesCardWhoseNextReviewDateIsInTheFuture() {
        Card card = persistCard(deck, "apple", "qua tao");
        persistProgress(owner, card, LocalDate.now().plusDays(5));

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void findDueCards_excludesCardFromOtherDeck() {
        Deck otherDeck = persistDeck(owner, "Deck khac");
        persistCard(otherDeck, "banana", "qua chuoi");

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void findDueCards_otherUsersProgressDoesNotHideCardFromThisUser() {
        User otherUser = persistUser("other", "other@lumora.test");
        Card card = persistCard(deck, "apple", "qua tao");

        persistProgress(otherUser, card, LocalDate.now().plusDays(30));

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    private User persistUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("khong-quan-trong-trong-test-nay")
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Deck persistDeck(User owner, String name) {
        Deck deck = Deck.builder()
                .name(name)
                .owner(owner)
                .isPublic(false)
                .build();
        return entityManager.persistAndFlush(deck);
    }

    private Card persistCard(Deck deck, String term, String definition) {
        Card card = Card.builder()
                .deck(deck)
                .term(term)
                .definition(definition)
                .build();
        return entityManager.persistAndFlush(card);
    }

    private CardProgress persistProgress(User user, Card card, LocalDate nextReviewDate) {
        CardProgress progress = CardProgress.builder()
                .user(user)
                .card(card)
                .status(ReviewStatus.LEARNING)
                .nextReviewDate(nextReviewDate)
                .build();
        return entityManager.persistAndFlush(progress);
    }
}
