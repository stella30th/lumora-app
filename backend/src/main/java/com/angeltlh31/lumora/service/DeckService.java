package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.deck.DeckOverviewResponse;
import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.ReviewLogRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DeckService {
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardProgressRepository cardProgressRepository;
    private final ReviewLogRepository reviewLogRepository;

    private final DeckAccessService deckAccessService;

    @Value("${review.new-cards-per-day:20}")
    private int newCardsPerDay = 20;

    public DeckResponse createDeck(Long ownerId, DeckRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id=" + ownerId));

        Deck deck = Deck.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .owner(owner)
                .build();

        return toResponse(deckRepository.save(deck));
    }

    @Transactional(readOnly = true)
    public Page<DeckResponse> getDecksByOwner(Long ownerId, Pageable pageable) {
        return deckRepository.findByOwnerId(ownerId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DeckOverviewResponse> getOverview(Long ownerId) {
        LocalDate today = LocalDate.now();
        Map<Long, Long> cardCounts = toLongMap(cardRepository.countCardsPerDeckOfOwner(ownerId), 1);

        Map<Long, Long> startedCounts = new HashMap<>();
        Map<Long, Long> dueReviewCounts = new HashMap<>();
        for (Object[] row : cardProgressRepository.progressStatsPerOwnDeck(ownerId, today)) {
            Long deckId = (Long) row[0];
            startedCounts.put(deckId, ((Number) row[1]).longValue());
            dueReviewCounts.put(deckId, row[2] == null ? 0 : ((Number) row[2]).longValue());
        }

        Map<Long, Long> introducedToday = new HashMap<>();
        for (Object[] row : reviewLogRepository.findCardsFirstReviewedSinceInOwnDecks(ownerId, today.atStartOfDay())) {
            introducedToday.merge((Long) row[0], 1L, Long::sum);
        }

        return deckRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(deck -> {
                    long cards = cardCounts.getOrDefault(deck.getId(), 0L);
                    long notStarted = cards - startedCounts.getOrDefault(deck.getId(), 0L);
                    long newQuota = Math.max(0, newCardsPerDay - introducedToday.getOrDefault(deck.getId(), 0L));
                    long due = dueReviewCounts.getOrDefault(deck.getId(), 0L) + Math.min(notStarted, newQuota);
                    return DeckOverviewResponse.builder()
                            .id(deck.getId())
                            .name(deck.getName())
                            .description(deck.getDescription())
                            .isPublic(deck.isPublic())
                            .ownerId(deck.getOwner().getId())
                            .createdAt(deck.getCreatedAt())
                            .cardCount(cards)
                            .dueCount(due)
                            .build();
                })
                .toList();
    }

    private static Map<Long, Long> toLongMap(List<Object[]> rows, int valueIndex) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[valueIndex]).longValue());
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Page<DeckResponse> getPublicDecks(Pageable pageable) {
        return deckRepository.findByIsPublicTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DeckResponse getDeckById(Long id, Long requesterId) {
        Deck deck = findDeckOrThrow(id);
        deckAccessService.verifyReadAccess(deck, requesterId);
        return toResponse(deck);
    }

    public DeckResponse updateDeck(Long id, Long ownerId, DeckRequest request) {
        Deck deck = findDeckOrThrow(id);
        deckAccessService.verifyOwnership(deck, ownerId);

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setPublic(request.isPublic());

        return toResponse(deck);
    }

    public void deleteDeck(Long id, Long ownerId) {
        Deck deck = findDeckOrThrow(id);
        deckAccessService.verifyOwnership(deck, ownerId);

        reviewLogRepository.deleteByDeckId(id);
        cardProgressRepository.deleteByDeckId(id);
        cardRepository.deleteByDeckId(id);
        deckRepository.deleteById(id);
    }

    private Deck findDeckOrThrow(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + id));
    }

    private DeckResponse toResponse(Deck deck) {
        return DeckResponse.builder()
                .id(deck.getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .isPublic(deck.isPublic())
                .ownerId(deck.getOwner().getId())
                .createdAt(deck.getCreatedAt())
                .build();
    }
}
