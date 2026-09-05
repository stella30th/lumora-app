package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewLog;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void spacedRepetitionSixRoundSequence_matchesDesignTable() {

        CardProgress progress = CardProgress.builder().build();

        service.applyReview(progress, true);
        assertAll("Luot 1",
                () -> assertEquals(1, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.6, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus()),
                () -> assertEquals(LocalDate.now().plusDays(1), progress.getNextReviewDate())
        );

        service.applyReview(progress, true);
        assertAll("Luot 2",
                () -> assertEquals(2, progress.getRepetitions().intValue()),
                () -> assertEquals(6, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.7, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );

        service.applyReview(progress, true);
        assertAll("Luot 3",
                () -> assertEquals(3, progress.getRepetitions().intValue()),
                () -> assertEquals(16, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.8, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );

        service.applyReview(progress, false);
        assertAll("Luot 4",
                () -> assertEquals(0, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.0, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus())
        );

        service.applyReview(progress, true);
        assertAll("Luot 5",
                () -> assertEquals(1, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.1, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus())
        );

        service.applyReview(progress, true);
        assertAll("Luot 6",
                () -> assertEquals(2, progress.getRepetitions().intValue()),
                () -> assertEquals(6, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.2, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );
    }

    @Test
    void easeFactor_neverGoesBelowMinimum_evenAfterManyWrongAnswersInARow() {

        CardProgress progress = CardProgress.builder().easeFactor(1.5).build();

        service.applyReview(progress, false);
        assertEquals(1.3, progress.getEaseFactor(), DELTA);

        service.applyReview(progress, false);
        assertEquals(1.3, progress.getEaseFactor(), DELTA);

        service.applyReview(progress, false);
        assertEquals(1.3, progress.getEaseFactor(), DELTA);
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

        CardProgressResponse response = service.submitReview(cardId, userId, true);

        assertAll("Response phan anh dung applyReview() cho 1 lan tra loi DUNG dau tien",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(1, response.getIntervalDays()),
                () -> assertEquals(2.6, response.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, response.getStatus()),
                () -> assertEquals(LocalDate.now().plusDays(1), response.getNextReviewDate())
        );

        verify(deckAccessService).verifyReadAccess(deck, userId);
        verify(cardProgressRepository).save(existingProgress);

        verify(reviewLogRepository).save(any(ReviewLog.class));

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

        CardProgressResponse response = service.submitReview(cardId, userId, true);

        assertAll("The moi toanh, tra loi DUNG lan dau - giong het 'Luot 1' o test applyReview()",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(2.6, response.getEaseFactor(), DELTA),
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
                () -> service.submitReview(cardId, 1L, true));

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
                () -> service.submitReview(cardId, userId, true));

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
    void getDueCards_deckNotFound_throwsResourceNotFoundException() {
        Long deckId = 6L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getDueCards(deckId, 1L));

        verifyNoInteractions(cardRepository, cardProgressRepository, deckAccessService);
    }
}
