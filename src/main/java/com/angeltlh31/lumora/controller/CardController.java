package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.card.CardRequest;
import com.angeltlh31.lumora.dto.card.CardResponse;
import com.angeltlh31.lumora.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Khong dat @RequestMapping o class vi co 2 nhom route khac nhau:
// /api/decks/{deckId}/cards (long theo Deck cha) va /api/cards/{id} (thao tac truc tiep tren 1 Card)
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // Ngay 9: them @AuthenticationPrincipal Long ownerId cho ca 3 endpoint GHI (create/update/
    // delete) - day la nguoi dang goi, se duoc CardService doi chieu voi chu Deck cha. (Ngay 9,
    // 2 endpoint DOC ben duoi con "ho": chi can token hop le la xem duoc Card cua Deck bat ky,
    // khong phan biet public/private - da xu ly o Ngay 10, xem comment truoc getCardsByDeck.)
    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId,
                                                     @AuthenticationPrincipal Long ownerId,
                                                     @Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.createCard(deckId, ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Ngay 10: them @AuthenticationPrincipal Long requesterId cho ca 2 endpoint DOC - truoc
    // Ngay 10, comment o day con ghi 2 endpoint nay "chua dung ownerId", gio da het con no do.
    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<CardResponse>> getCardsByDeck(@PathVariable Long deckId,
                                                              @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardService.getCardsByDeck(deckId, requesterId));
    }

    @GetMapping("/api/cards/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardService.getCardById(id, requesterId));
    }

    @PutMapping("/api/cards/{id}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long ownerId,
                                                     @Valid @RequestBody CardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(id, ownerId, request));
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                            @AuthenticationPrincipal Long ownerId) {
        cardService.deleteCard(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
