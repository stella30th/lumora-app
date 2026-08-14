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

    private final DeckRepository deckRepository; //DI
    private final UserRepository userRepository;

    public DeckResponse createDeck(Long ownerId, DeckRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay User id=" + ownerId));

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
    public DeckResponse getDeckById(Long id) {
        return toResponse(findDeckOrThrow(id));
    }

    public DeckResponse updateDeck(Long id, DeckRequest request) {
        Deck deck = findDeckOrThrow(id);
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setPublic(request.isPublic());
        // Khong goi deckRepository.save(deck) - xem giai thich "dirty checking" ben duoi.
        return toResponse(deck);
    }

    public void deleteDeck(Long id) {
        deckRepository.delete(findDeckOrThrow(id));
    }

    private Deck findDeckOrThrow(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Deck id=" + id));
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
