package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.deck.DeckOverviewResponse;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceOverviewTest {

    @Mock private DeckRepository deckRepository;
    @Mock private UserRepository userRepository;
    @Mock private CardRepository cardRepository;
    @Mock private CardProgressRepository cardProgressRepository;
    @Mock private ReviewLogRepository reviewLogRepository;
    @Mock private DeckAccessService deckAccessService;

    @InjectMocks
    private DeckService deckService;

    @Test
    void overview_countsDueReviewsPlusNewCardsWithinDailyQuota() {
        User owner = User.builder().id(1L).build();
        Deck big = Deck.builder().id(10L).name("Big").owner(owner).build();
        Deck empty = Deck.builder().id(11L).name("Empty").owner(owner).build();
        when(deckRepository.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(big, empty));

        List<Object[]> cardCounts = new ArrayList<>();
        cardCounts.add(new Object[]{10L, 100L});
        when(cardRepository.countCardsPerDeckOfOwner(1L)).thenReturn(cardCounts);

        List<Object[]> progress = new ArrayList<>();
        progress.add(new Object[]{10L, 30L, 4L});
        when(cardProgressRepository.progressStatsPerOwnDeck(eq(1L), any())).thenReturn(progress);

        List<Object[]> introducedToday = new ArrayList<>();
        for (long cardId = 1; cardId <= 15; cardId++) {
            introducedToday.add(new Object[]{10L, cardId});
        }
        when(reviewLogRepository.findCardsFirstReviewedSinceInOwnDecks(eq(1L), any())).thenReturn(introducedToday);

        List<DeckOverviewResponse> overview = deckService.getOverview(1L);

        assertEquals(2, overview.size());
        assertEquals(100, overview.get(0).getCardCount());
        assertEquals(4 + 5, overview.get(0).getDueCount(),
                "4 reviews due + min(70 unseen cards, 20 - 15 already introduced today)");
        assertEquals(0, overview.get(1).getCardCount());
        assertEquals(0, overview.get(1).getDueCount());
    }
}
