package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.service.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    // TODO: ownerId dang truyen tam qua query param.
    // Khi hoc Spring Security se thay bang cach lay tu JWT token (Principal),
    // luc do client khong con phai tu gui ownerId nua (tranh gia mao chiem deck nguoi khac).
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@RequestParam Long ownerId,
                                                    @Valid @RequestBody DeckRequest request) {
        DeckResponse response = deckService.createDeck(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> getMyDecks(@RequestParam Long ownerId) {
        return ResponseEntity.ok(deckService.getDecksByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeckById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long id,
                                                    @Valid @RequestBody DeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
