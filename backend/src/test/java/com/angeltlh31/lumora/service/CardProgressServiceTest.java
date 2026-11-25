package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewLog;
import com.angeltlh31.lumora.entity.ReviewRating;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ForbiddenException;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardProgressServiceTest {
    private static final double DELTA = 0.0001;

    @Mock
    private CardRepository cardRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardProgressRepository cardProgressRepository;
    @Mock
    private DeckAccessService deckAccessService;

    @Mock
    private ReviewLogRepository reviewLogRepository;

    @InjectMocks
    private CardProgressService service;

    @Test
    void mixedRatingSequence_matchesDesignTable() {
        CardProgress progress = CardProgress.builder().build();

        service.applyReview(progress, ReviewRating.GOOD);
        assertState("Luot 1 GOOD", progress, 1, 1, 2.5, ReviewStatus.LEARNING);

        service.applyReview(progress, ReviewRating.GOOD);
        assertState("Luot 2 GOOD", progress, 2, 6, 2.5, ReviewStatus.REVIEW);

        service.applyReview(progress, ReviewRating.GOOD);
        assertState("Luot 3 GOOD: 6 x 2.5", progress, 3, 15, 2.5, ReviewStatus.REVIEW);

        service.applyReview(progress, ReviewRating.HARD);
        assertState("Luot 4 HARD: 15 x 1.2", progress, 4, 18, 2.35, ReviewStatus.REVIEW);

        service.applyReview(progress, ReviewRating.EASY);
        assertState("Luot 5 EASY: 18 x 2.35 x 1.3", progress, 5, 55, 2.5, ReviewStatus.REVIEW);

        service.applyReview(progress, ReviewRating.AGAIN);
        assertState("Luot 6 AGAIN: on lai ngay hom nay", progress, 0, 0, 2.3, ReviewStatus.LEARNING);

        service.applyReview(progress, ReviewRating.GOOD);
        assertState("Luot 7 GOOD sau khi quen: hoc lai tu dau", progress, 1, 1, 2.3, ReviewStatus.LEARNING);
    }

    @Test
    void good_doesNotChangeEase_soEaseNoLongerGrowsWithoutBound() {
        CardProgress progress = CardProgress.builder().build();

        for (int i = 0; i < 10; i++) {
            service.applyReview(progress, ReviewRating.GOOD);
        }

        assertEquals(2.5, progress.getEaseFactor(), DELTA);
    }

    @Test
    void easyRepeatedly_capsEaseAndInterval() {
        CardProgress progress = CardProgress.builder().build();

        service.applyReview(progress, ReviewRating.EASY);
        assertState("EASY lan 1", progress, 1, 4, 2.65, ReviewStatus.LEARNING);
        service.applyReview(progress, ReviewRating.EASY);
        assertState("EASY lan 2: 6 x 1.3", progress, 2, 8, 2.8, ReviewStatus.REVIEW);
        service.applyReview(progress, ReviewRating.EASY);
        assertState("EASY lan 3: 8 x 2.8 x 1.3", progress, 3, 29, 2.95, ReviewStatus.REVIEW);
        service.applyReview(progress, ReviewRating.EASY);
        assertState("EASY lan 4: ease cham tran 3.0", progress, 4, 111,
                CardProgressService.MAX_EASE_FACTOR, ReviewStatus.REVIEW);
        service.applyReview(progress, ReviewRating.EASY);
        assertState("EASY lan 5: khoang cach cham tran 365", progress, 5,
                CardProgressService.MAX_INTERVAL_DAYS, CardProgressService.MAX_EASE_FACTOR, ReviewStatus.REVIEW);
    }

    @Test
    void hard_isLongerThanPreviousInterval_butNotLongerThanGood() {
        CardProgress hard = CardProgress.builder().build();
        CardProgress good = CardProgress.builder().build();
        service.applyReview(hard, ReviewRating.GOOD);
        service.applyReview(good, ReviewRating.GOOD);

        service.applyReview(hard, ReviewRating.HARD);
        service.applyReview(good, ReviewRating.GOOD);

        assertEquals(2, hard.getIntervalDays().intValue());
        assertTrue(hard.getIntervalDays() <= good.getIntervalDays());
    }

    @Test
    void easeFactor_neverGoesBelowMinimum_evenAfterManyAgainInARow() {
        CardProgress progress = CardProgress.builder().easeFactor(1.4).build();

        service.applyReview(progress, ReviewRating.AGAIN);
        assertEquals(CardProgressService.MIN_EASE_FACTOR, progress.getEaseFactor(), DELTA);

        service.applyReview(progress, ReviewRating.AGAIN);
        service.applyReview(progress, ReviewRating.HARD);
        assertEquals(CardProgressService.MIN_EASE_FACTOR, progress.getEaseFactor(), DELTA);
    }

    private void assertState(String label, CardProgress p, int reps, int interval, double ease, ReviewStatus status) {
        assertAll(label,
                () -> assertEquals(reps, p.getRepetitions().intValue(), "repetitions"),
                () -> assertEquals(interval, p.getIntervalDays().intValue(), "intervalDays"),
                () -> assertEquals(ease, p.getEaseFactor(), DELTA, "easeFactor"),
                () -> assertEquals(status, p.getStatus(), "status"),
                () -> assertEquals(LocalDate.now().plusDays(interval), p.getNextReviewDate(), "nextReviewDate"));
    }

    @Test
    void submitReview_existingProgress_correctAnswer_updatesAndSaves() {
        Long cardId = 10L;
        Long userId = 1L;
        User owner = User.builder().id(userId).build();
        Deck deck = Deck.builder().id(2L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();

        CardProgress existingProgress = CardProgress.builder()
                .id(99L)
                .user(owner)
                .card(card)
                .easeFactor(2.5)
                .intervalDays(0)
                .repetitions(0)
                .status(ReviewStatus.NEW)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardProgressRepository.findByUserIdAndCardId(userId, cardId))
                .thenReturn(Optional.of(existingProgress));

        when(cardProgressRepository.save(any(CardProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardProgressResponse response = service.submitReview(cardId, userId, ReviewRating.GOOD);

        assertAll("Response phan anh dung applyReview() cho 1 lan tra loi DUNG dau tien",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(1, response.getIntervalDays()),
                () -> assertEquals(2.5, response.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, response.getStatus()),
                () -> assertEquals(LocalDate.now().plusDays(1), response.getNextReviewDate())
        );

        verify(deckAccessService).verifyReadAccess(deck, userId);
        verify(cardProgressRepository).save(existingProgress);

        ArgumentCaptor<ReviewLog> logCaptor = ArgumentCaptor.forClass(ReviewLog.class);
        verify(reviewLogRepository).save(logCaptor.capture());
        assertEquals(ReviewRating.GOOD, logCaptor.getValue().getRating());
        assertTrue(logCaptor.getValue().isCorrect());

        verify(userRepository, never()).findById(any());
    }

    @Test
    void submitReview_noExistingProgress_createsNewProgressFromUser() {
        Long cardId = 20L;
        Long userId = 1L;
        User owner = User.builder().id(userId).build();
        Deck deck = Deck.builder().id(3L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        when(cardProgressRepository.findByUserIdAndCardId(userId, cardId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(cardProgressRepository.save(any(CardProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardProgressResponse response = service.submitReview(cardId, userId, ReviewRating.GOOD);

        assertAll("The moi toanh, tra loi DUNG lan dau - giong het 'Luot 1' o test applyReview()",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(2.5, response.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, response.getStatus())
        );

        ArgumentCaptor<CardProgress> savedCaptor = ArgumentCaptor.forClass(CardProgress.class);
        verify(cardProgressRepository).save(savedCaptor.capture());
        assertEquals(owner, savedCaptor.getValue().getUser());
        assertEquals(card, savedCaptor.getValue().getCard());
        verify(userRepository).findById(userId);
    }

    @Test
    void submitReview_cardNotFound_throwsResourceNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.submitReview(cardId, 1L, ReviewRating.GOOD));

        verifyNoInteractions(deckAccessService, cardProgressRepository);
    }

    @Test
    void submitReview_noReadAccess_throwsForbiddenException() {
        Long cardId = 30L;
        Long userId = 2L;
        User owner = User.builder().id(1L).build();
        Deck deck = Deck.builder().id(4L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        doThrow(new ForbiddenException("Khong co quyen xem Deck id=" + deck.getId()))
                .when(deckAccessService).verifyReadAccess(deck, userId);

        assertThrows(ForbiddenException.class,
                () -> service.submitReview(cardId, userId, ReviewRating.GOOD));

        verifyNoInteractions(cardProgressRepository);
    }

    @Test
    void getDueCards_mixesCardsWithAndWithoutExistingProgress() {
        Long deckId = 5L;
        Long requesterId = 1L;
        User owner = User.builder().id(requesterId).build();
        Deck deck = Deck.builder().id(deckId).owner(owner).isPublic(false).build();
        Card cardWithProgress = Card.builder().id(100L).deck(deck)
                .term("apple").definition("qua tao").build();
        Card cardWithoutProgress = Card.builder().id(101L).deck(deck)
                .term("banana").definition("qua chuoi").build();
        CardProgress progress = CardProgress.builder()
                .card(cardWithProgress)
                .status(ReviewStatus.REVIEW)
                .nextReviewDate(LocalDate.now())
                .build();

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        when(cardRepository.findDueCards(eq(deckId), eq(requesterId), any(LocalDate.class)))
                .thenReturn(List.of(cardWithProgress, cardWithoutProgress));

        when(cardProgressRepository.findByUserIdAndCardIdIn(eq(requesterId), anyList()))
                .thenReturn(List.of(progress));

        List<DueCardResponse> result = service.getDueCards(deckId, requesterId);

        assertEquals(2, result.size());
        DueCardResponse withProgress = result.stream()
                .filter(r -> r.getId().equals(100L)).findFirst().orElseThrow();
        DueCardResponse withoutProgress = result.stream()
                .filter(r -> r.getId().equals(101L)).findFirst().orElseThrow();

        assertAll("The DA co CardProgress -> lay status/nextReviewDate tu progress that",
                () -> assertEquals(ReviewStatus.REVIEW, withProgress.getStatus()),
                () -> assertEquals(LocalDate.now(), withProgress.getNextReviewDate())
        );

        assertAll("The CHUA TUNG on (progress == null trong toDueResponse) -> mac dinh NEW",
                () -> assertEquals(ReviewStatus.NEW, withoutProgress.getStatus()),
                () -> assertNull(withoutProgress.getNextReviewDate())
        );
    }

    @Test
    void getDueCards_limitsNewCardsToRemainingDailyQuota_butKeepsAllReviews() {
        Long deckId = 7L;
        Long userId = 1L;
        User owner = User.builder().id(userId).build();
        Deck deck = Deck.builder().id(deckId).owner(owner).isPublic(false).build();

        List<Card> due = new ArrayList<>();
        List<CardProgress> progresses = new ArrayList<>();
        for (long id = 1; id <= 3; id++) {
            Card card = Card.builder().id(id).deck(deck).term("r" + id).definition("d").build();
            due.add(card);
            progresses.add(CardProgress.builder().card(card).status(ReviewStatus.REVIEW)
                    .nextReviewDate(LocalDate.now().minusDays(id)).build());
        }
        for (long id = 100; id < 125; id++) {
            due.add(Card.builder().id(id).deck(deck).term("n" + id).definition("d").build());
        }

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(cardRepository.findDueCards(eq(deckId), eq(userId), any(LocalDate.class))).thenReturn(due);
        when(cardProgressRepository.findByUserIdAndCardIdIn(eq(userId), anyList())).thenReturn(progresses);
        when(reviewLogRepository.findCardIdsFirstReviewedSince(eq(userId), eq(deckId), any()))
                .thenReturn(LongStream.range(500, 515).boxed().toList());

        List<DueCardResponse> result = service.getDueCards(deckId, userId);

        assertEquals(3 + 5, result.size());
        assertEquals(List.of(3L, 2L, 1L), result.subList(0, 3).stream().map(DueCardResponse::getId).toList(),
                "The on lai dung truoc, qua han lau nhat len dau");
        assertEquals(List.of(100L, 101L, 102L, 103L, 104L),
                result.subList(3, 8).stream().map(DueCardResponse::getId).toList());
    }

    @Test
    void getDueCards_dailyQuotaUsedUp_returnsNoNewCards() {
        Long deckId = 8L;
        User owner = User.builder().id(1L).build();
        Deck deck = Deck.builder().id(deckId).owner(owner).isPublic(false).build();
        Card newCard = Card.builder().id(1L).deck(deck).term("t").definition("d").build();

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(cardRepository.findDueCards(eq(deckId), eq(1L), any(LocalDate.class))).thenReturn(List.of(newCard));
        when(reviewLogRepository.findCardIdsFirstReviewedSince(eq(1L), eq(deckId), any()))
                .thenReturn(LongStream.range(0, 20).boxed().toList());

        assertTrue(service.getDueCards(deckId, 1L).isEmpty());
    }

    @Test
    void getDueCards_deckNotFound_throwsResourceNotFoundException() {
        Long deckId = 6L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getDueCards(deckId, 1L));

        verifyNoInteractions(cardRepository, cardProgressRepository, deckAccessService);
    }
}
