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
    // delete) - day la nguoi dang goi, se duoc CardService doi chieu voi chu Deck cha. 2 endpoint
    // DOC (getCardsByDeck/getCardById) co tinh CHUA dung ownerId: van bat buoc phai co token hop
    // le moi goi duoc (SecurityConfig.anyRequest().authenticated() tu ngay 7), nhung chua gioi han
    // ai cung xem duoc Card cua Deck bat ky - xem giai thich trong docs/recap-day9.md.
    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId,
                                                     @AuthenticationPrincipal Long ownerId,
                                                     @Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.createCard(deckId, ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<CardResponse>> getCardsByDeck(@PathVariable Long deckId) {
        return ResponseEntity.ok(cardService.getCardsByDeck(deckId));
    }

    @GetMapping("/api/cards/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
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
