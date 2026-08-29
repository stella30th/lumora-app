package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.card.CardRequest;
import com.angeltlh31.lumora.dto.card.CardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.exception.ForbiddenException;
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

    // Ngay 9: Card khong co cot owner rieng - "chu so huu" cua 1 Card la chu so huu cua
    // Deck chua no (deck.getOwner()). Vi vay createCard nhan them ownerId va phai DI QUA
    // Deck moi biet duoc co cho phep tao Card trong do khong.
    public CardResponse createCard(Long deckId, Long ownerId, CardRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Deck id=" + deckId));
        verifyDeckOwnership(deck, ownerId);

        Card card = Card.builder()
                .term(request.getTerm())
                .definition(request.getDefinition())
                .deck(deck)
                .build();

        return toResponse(cardRepository.save(card));
    }

    // Ngay 10: Card khong co isPublic rieng - phai lay Deck cha ra roi ap dung quyen doc cua
    // Deck (giong het cach Ngay 9 xu ly quyen GHI qua Deck cha). Phai query Deck truoc de lay
    // duoc trang thai isPublic/owner ma kiem tra, khong the chi dua vao deckId nhu truoc.
    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByDeck(Long deckId, Long requesterId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Deck id=" + deckId));
        verifyReadAccess(deck, requesterId);

        return cardRepository.findByDeckId(deckId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id, Long requesterId) {
        Card card = findCardOrThrow(id);
        verifyReadAccess(card.getDeck(), requesterId);
        return toResponse(card);
    }

    public CardResponse updateCard(Long id, Long ownerId, CardRequest request) {
        Card card = findCardOrThrow(id);
        verifyDeckOwnership(card.getDeck(), ownerId);

        card.setTerm(request.getTerm());
        card.setDefinition(request.getDefinition());
        // Khong goi save() - dirty checking tu lo, giong DeckService.updateDeck
        return toResponse(card);
    }

    public void deleteCard(Long id, Long ownerId) {
        Card card = findCardOrThrow(id);
        verifyDeckOwnership(card.getDeck(), ownerId);
        cardRepository.delete(card);
    }

    private Card findCardOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Card id=" + id));
    }

    // Cung 1 kieu kiem tra nhu DeckService.verifyOwnership, chi khac o cho "tai nguyen" o day
    // la Deck cha cua Card (deck da duoc load san tu findCardOrThrow/deckRepository.findById
    // ben tren, khong query them lan nua).
    private void verifyDeckOwnership(Deck deck, Long ownerId) {
        if (!deck.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException(
                    "Ban khong co quyen thao tac tren Deck id=" + deck.getId());
        }
    }

    // Ngay 10: ban sao co chu y cua DeckService.verifyReadAccess - CHUA gop chung thanh 1
    // class dung chung (vd AuthorizationService) vi hien tai chi co 2 noi dung logic nay va
    // gop som se lam tang do phuc tap khong can thiet ("premature abstraction"). Neu sau nay co
    // them resource thu 3 can kieu quyen doc tuong tu, day se la luc nen tach ra.
    private void verifyReadAccess(Deck deck, Long requesterId) {
        boolean isOwner = deck.getOwner().getId().equals(requesterId);
        if (!deck.isPublic() && !isOwner) {
            throw new ForbiddenException(
                    "Ban khong co quyen xem Deck id=" + deck.getId());
        }
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
