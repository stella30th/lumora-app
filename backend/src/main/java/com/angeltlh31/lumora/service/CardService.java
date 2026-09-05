package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.card.CardRequest;
import com.angeltlh31.lumora.dto.card.CardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    private final DeckAccessService deckAccessService;

    public CardResponse createCard(Long deckId, Long ownerId, CardRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + deckId));
        deckAccessService.verifyOwnership(deck, ownerId);

        Card card = Card.builder()
                .term(request.getTerm())
                .definition(request.getDefinition())
                .deck(deck)
                .build();

        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByDeck(Long deckId, Long requesterId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + deckId));
        deckAccessService.verifyReadAccess(deck, requesterId);

        return cardRepository.findByDeckId(deckId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id, Long requesterId) {
        Card card = findCardOrThrow(id);
        deckAccessService.verifyReadAccess(card.getDeck(), requesterId);
        return toResponse(card);
    }

    public CardResponse updateCard(Long id, Long ownerId, CardRequest request) {
        Card card = findCardOrThrow(id);
        deckAccessService.verifyOwnership(card.getDeck(), ownerId);

        card.setTerm(request.getTerm());
        card.setDefinition(request.getDefinition());

        return toResponse(card);
    }

    public void deleteCard(Long id, Long ownerId) {
        Card card = findCardOrThrow(id);
        deckAccessService.verifyOwnership(card.getDeck(), ownerId);
        cardRepository.delete(card);
    }

    private Card findCardOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id=" + id));
    }

    private CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .term(card.getTerm())
                .definition(card.getDefinition())
                .deckId(card.getDeck().getId())
                .createdAt(card.getCreatedAt())
                .build();
    }
}
