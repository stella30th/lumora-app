package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    private final DeckAccessService deckAccessService;

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
    public List<DeckResponse> getDecksByOwner(Long ownerId) {
        return deckRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> getPublicDecks() {
        return deckRepository.findByIsPublicTrue()
                .stream()
                .map(this::toResponse)
                .toList();
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
        deckRepository.delete(deck);
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
