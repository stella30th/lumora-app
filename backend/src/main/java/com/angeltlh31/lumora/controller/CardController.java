package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.card.CardRequest;
import com.angeltlh31.lumora.dto.card.CardResponse;
import com.angeltlh31.lumora.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Tao Card moi trong 1 Deck (chi chu so huu Deck cha)")
    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId,
                                                     @AuthenticationPrincipal Long ownerId,
                                                     @Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.createCard(deckId, ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lay danh sach Card trong 1 Deck (yeu cau Deck public hoac la chu so huu)")
    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<CardResponse>> getCardsByDeck(@PathVariable Long deckId,
                                                              @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardService.getCardsByDeck(deckId, requesterId));
    }

    @Operation(summary = "Xem chi tiet 1 Card (yeu cau Deck cha public hoac la chu so huu)")
    @GetMapping("/api/cards/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardService.getCardById(id, requesterId));
    }

    @Operation(summary = "Cap nhat Card (chi chu so huu Deck cha)")
    @PutMapping("/api/cards/{id}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long ownerId,
                                                     @Valid @RequestBody CardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(id, ownerId, request));
    }

    @Operation(summary = "Xoa Card (chi chu so huu Deck cha)")
    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                            @AuthenticationPrincipal Long ownerId) {
        cardService.deleteCard(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
