package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewLog;
import com.angeltlh31.lumora.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ReviewLogRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private TestEntityManager em;
    @Autowired private ReviewLogRepository reviewLogRepository;

    @Test
    void dateQueries_returnLocalDates_groupedPerDay() {
        User user = em.persistAndFlush(User.builder().username("u").email("u@test.local").passwordHash("x").build());
        Deck deck = em.persistAndFlush(Deck.builder().name("d").owner(user).build());
        Card card = em.persistAndFlush(Card.builder().deck(deck).term("t").definition("d").build());
        LocalDate today = LocalDate.now();
        log(user, card, today.atTime(9, 0), true);
        log(user, card, today.atTime(10, 0), false);
        log(user, card, today.minusDays(1).atTime(23, 30), true);

        List<LocalDate> days = reviewLogRepository.findDistinctReviewDatesByUserId(user.getId());
        List<Object[]> grouped = reviewLogRepository.countGroupedByDateAndCorrect(
                user.getId(), today.minusDays(7).atStartOfDay());

        assertEquals(List.of(today, today.minusDays(1)), days);
        assertEquals(3, grouped.size());
        LocalDate first = (LocalDate) grouped.get(0)[0];
        assertEquals(LocalDate.class, first.getClass());
    }

    private void log(User user, Card card, LocalDateTime at, boolean correct) {
        em.persistAndFlush(ReviewLog.builder().user(user).card(card).correct(correct).reviewedAt(at).build());
    }
}
