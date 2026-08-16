package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.card.CardRequest;
import com.angeltlh31.lumora.dto.card.CardResponse;
import com.angeltlh31.lumora.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Khong dat @RequestMapping o class vi co 2 nhom route khac nhau:
// /api/decks/{deckId}/cards (long theo Deck cha) va /api/cards/{id} (thao tac truc tiep tren 1 Card)
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId,
                                                     @Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.createCard(deckId, request);
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
                                                     @Valid @RequestBody CardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(id, request));
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
