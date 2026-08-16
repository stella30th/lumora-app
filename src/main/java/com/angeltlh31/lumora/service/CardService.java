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

    public CardResponse createCard(Long deckId, CardRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Deck id=" + deckId));

        Card card = Card.builder()
                .term(request.getTerm())
                .definition(request.getDefinition())
                .deck(deck)
                .build();

        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByDeck(Long deckId) {
        return cardRepository.findByDeckId(deckId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        return toResponse(findCardOrThrow(id));
    }

    public CardResponse updateCard(Long id, CardRequest request) {
        Card card = findCardOrThrow(id);
        card.setTerm(request.getTerm());
        card.setDefinition(request.getDefinition());
        // Khong goi save() - dirty checking tu lo, giong DeckService.updateDeck
        return toResponse(card);
    }

    public void deleteCard(Long id) {
        cardRepository.delete(findCardOrThrow(id));
    }

    private Card findCardOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Card id=" + id));
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
