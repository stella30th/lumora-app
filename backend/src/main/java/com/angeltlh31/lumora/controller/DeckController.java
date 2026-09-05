package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.service.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @Operation(summary = "Tao Deck moi cho user dang dang nhap")
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@AuthenticationPrincipal Long ownerId,
                                                    @Valid @RequestBody DeckRequest request) {
        DeckResponse response = deckService.createDeck(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Lay danh sach Deck cua user dang dang nhap")
    @GetMapping
    public ResponseEntity<List<DeckResponse>> getMyDecks(@AuthenticationPrincipal Long ownerId) {
        return ResponseEntity.ok(deckService.getDecksByOwner(ownerId));
    }

    @Operation(summary = "Lay danh sach Deck cong khai (khong loc theo chu so huu)")
    @GetMapping("/public")
    public ResponseEntity<List<DeckResponse>> getPublicDecks() {
        return ResponseEntity.ok(deckService.getPublicDecks());
    }

    @Operation(summary = "Xem chi tiet 1 Deck (yeu cau Deck public hoac la chu so huu)")
    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(deckService.getDeckById(id, requesterId));
    }

    @Operation(summary = "Cap nhat Deck (chi chu so huu)")
    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long id,
                                                    @AuthenticationPrincipal Long ownerId,
                                                    @Valid @RequestBody DeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, ownerId, request));
    }

    @Operation(summary = "Xoa Deck (chi chu so huu)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id,
                                            @AuthenticationPrincipal Long ownerId) {
        deckService.deleteDeck(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
