package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.ReviewLog;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CardProgressService {

    private static final double EASE_BONUS_CORRECT = 0.1;
    private static final double EASE_PENALTY_WRONG = 0.8;
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final int GRADUATE_REPETITIONS = 2;

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CardProgressRepository cardProgressRepository;
    private final DeckAccessService deckAccessService;

    private final ReviewLogRepository reviewLogRepository;

    public CardProgressResponse submitReview(Long cardId, Long userId, boolean correct) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id=" + cardId));
        deckAccessService.verifyReadAccess(card.getDeck(), userId);

        CardProgress progress = cardProgressRepository.findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> createNewProgress(userId, card));

        applyReview(progress, correct);
        CardProgress saved = cardProgressRepository.save(progress);

        reviewLogRepository.save(ReviewLog.builder()
                .user(progress.getUser())
                .card(card)
                .correct(correct)
                .reviewedAt(progress.getLastReviewedAt())
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DueCardResponse> getDueCards(Long deckId, Long requesterId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + deckId));
        deckAccessService.verifyReadAccess(deck, requesterId);

        List<Card> dueCards = cardRepository.findDueCards(deckId, requesterId, LocalDate.now());
        List<Long> cardIds = dueCards.stream().map(Card::getId).toList();

        Map<Long, CardProgress> progressByCardId = cardProgressRepository
                .findByUserIdAndCardIdIn(requesterId, cardIds)
                .stream()
                .collect(Collectors.toMap(cp -> cp.getCard().getId(), cp -> cp));

        return dueCards.stream()
                .map(card -> toDueResponse(card, progressByCardId.get(card.getId())))
                .toList();
    }

    void applyReview(CardProgress progress, boolean correct) {
        if (correct) {
            progress.setRepetitions(progress.getRepetitions() + 1);
            progress.setIntervalDays(nextInterval(progress));
            progress.setEaseFactor(progress.getEaseFactor() + EASE_BONUS_CORRECT);
            progress.setStatus(progress.getRepetitions() >= GRADUATE_REPETITIONS
                    ? ReviewStatus.REVIEW
                    : ReviewStatus.LEARNING);
        } else {
            progress.setRepetitions(0);
            progress.setIntervalDays(1);
            progress.setEaseFactor(Math.max(progress.getEaseFactor() - EASE_PENALTY_WRONG, MIN_EASE_FACTOR));
            progress.setStatus(ReviewStatus.LEARNING);
        }
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setNextReviewDate(LocalDate.now().plusDays(progress.getIntervalDays()));
    }

    private int nextInterval(CardProgress progress) {
        return switch (progress.getRepetitions()) {
            case 1 -> 1;
            case 2 -> 6;
            default -> (int) Math.round(progress.getIntervalDays() * progress.getEaseFactor());
        };
    }

    private CardProgress createNewProgress(Long userId, Card card) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id=" + userId));

        return CardProgress.builder()
                .user(user)
                .card(card)
                .build();
    }

    private CardProgressResponse toResponse(CardProgress progress) {
        return CardProgressResponse.builder()
                .id(progress.getId())
                .cardId(progress.getCard().getId())
                .easeFactor(progress.getEaseFactor())
                .intervalDays(progress.getIntervalDays())
                .repetitions(progress.getRepetitions())
                .status(progress.getStatus())
                .nextReviewDate(progress.getNextReviewDate())
                .lastReviewedAt(progress.getLastReviewedAt())
                .build();
    }

    private DueCardResponse toDueResponse(Card card, CardProgress progress) {
        return DueCardResponse.builder()
                .id(card.getId())
                .term(card.getTerm())
                .definition(card.getDefinition())
                .deckId(card.getDeck().getId())
                .status(progress != null ? progress.getStatus() : ReviewStatus.NEW)
                .nextReviewDate(progress != null ? progress.getNextReviewDate() : null)
                .build();
    }
}
