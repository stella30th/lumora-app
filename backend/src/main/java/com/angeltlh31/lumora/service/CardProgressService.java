package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewRating;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CardProgressService {
    static final double MIN_EASE_FACTOR = 1.3;
    static final double MAX_EASE_FACTOR = 3.0;
    private static final double EASE_DELTA_AGAIN = -0.20;
    private static final double EASE_DELTA_HARD = -0.15;
    private static final double EASE_DELTA_EASY = 0.15;
    private static final double HARD_INTERVAL_MULTIPLIER = 1.2;
    private static final double EASY_BONUS = 1.3;

    static final int MAX_INTERVAL_DAYS = 365;
    private static final int GRADUATE_REPETITIONS = 2;

    @Value("${review.new-cards-per-day:20}")
    private int newCardsPerDay = 20;

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CardProgressRepository cardProgressRepository;
    private final DeckAccessService deckAccessService;

    private final ReviewLogRepository reviewLogRepository;

    public CardProgressResponse submitReview(Long cardId, Long userId, ReviewRating rating) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id=" + cardId));
        deckAccessService.verifyReadAccess(card.getDeck(), userId);

        CardProgress progress = cardProgressRepository.findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> createNewProgress(userId, card));

        applyReview(progress, rating);
        CardProgress saved = cardProgressRepository.save(progress);

        reviewLogRepository.save(ReviewLog.builder()
                .user(progress.getUser())
                .card(card)
                .correct(rating != ReviewRating.AGAIN)
                .rating(rating)
                .reviewedAt(progress.getLastReviewedAt())
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DueCardResponse> getDueCards(Long deckId, Long requesterId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + deckId));
        deckAccessService.verifyReadAccess(deck, requesterId);

        LocalDate today = LocalDate.now();
        List<Card> dueCards = cardRepository.findDueCards(deckId, requesterId, today);
        List<Long> cardIds = dueCards.stream().map(Card::getId).toList();

        Map<Long, CardProgress> progressByCardId = cardProgressRepository
                .findByUserIdAndCardIdIn(requesterId, cardIds)
                .stream()
                .collect(Collectors.toMap(cp -> cp.getCard().getId(), cp -> cp));

        List<DueCardResponse> reviews = dueCards.stream()
                .filter(card -> progressByCardId.containsKey(card.getId()))
                .sorted(Comparator
                        .comparing((Card card) -> progressByCardId.get(card.getId()).getNextReviewDate())
                        .thenComparing(Card::getId))
                .map(card -> toDueResponse(card, progressByCardId.get(card.getId())))
                .toList();

        int introducedToday = reviewLogRepository
                .findCardIdsFirstReviewedSince(requesterId, deckId, today.atStartOfDay())
                .size();
        int remainingNew = Math.max(0, newCardsPerDay - introducedToday);
        List<DueCardResponse> newCards = dueCards.stream()
                .filter(card -> !progressByCardId.containsKey(card.getId()))
                .sorted(Comparator.comparing(Card::getId))
                .limit(remainingNew)
                .map(card -> toDueResponse(card, null))
                .toList();

        List<DueCardResponse> queue = new ArrayList<>(reviews);
        queue.addAll(newCards);
        return queue;
    }

    void applyReview(CardProgress progress, ReviewRating rating) {
        int prevInterval = progress.getIntervalDays();
        double ease = progress.getEaseFactor();

        if (rating == ReviewRating.AGAIN) {
            progress.setRepetitions(0);
            progress.setIntervalDays(0);
            progress.setEaseFactor(clampEase(ease + EASE_DELTA_AGAIN));
            progress.setStatus(ReviewStatus.LEARNING);
        } else {
            int repetitions = progress.getRepetitions() + 1;
            progress.setRepetitions(repetitions);
            progress.setIntervalDays(nextInterval(rating, repetitions, prevInterval, ease));
            double easeDelta = switch (rating) {
                case HARD -> EASE_DELTA_HARD;
                case EASY -> EASE_DELTA_EASY;
                default -> 0;
            };
            progress.setEaseFactor(clampEase(ease + easeDelta));
            progress.setStatus(repetitions >= GRADUATE_REPETITIONS
                    ? ReviewStatus.REVIEW
                    : ReviewStatus.LEARNING);
        }
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setNextReviewDate(LocalDate.now().plusDays(progress.getIntervalDays()));
    }

    private int nextInterval(ReviewRating rating, int repetitions, int prevInterval, double ease) {
        double raw = switch (rating) {
            case HARD -> repetitions == 1 ? 1 : prevInterval * HARD_INTERVAL_MULTIPLIER;
            case GOOD -> switch (repetitions) {
                case 1 -> 1;
                case 2 -> 6;
                default -> prevInterval * ease;
            };
            case EASY -> switch (repetitions) {
                case 1 -> 4;
                case 2 -> 6 * EASY_BONUS;
                default -> prevInterval * ease * EASY_BONUS;
            };
            case AGAIN -> throw new IllegalArgumentException("AGAIN is handled separately");
        };
        int interval = (int) Math.round(raw);
        if (repetitions > 1) {
            interval = Math.max(interval, prevInterval + 1);
        }
        return Math.min(Math.max(interval, 1), MAX_INTERVAL_DAYS);
    }

    private static double clampEase(double ease) {
        double rounded = Math.round(ease * 100) / 100.0;
        return Math.min(Math.max(rounded, MIN_EASE_FACTOR), MAX_EASE_FACTOR);
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
