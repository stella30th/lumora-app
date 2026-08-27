package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.service.DeckService;
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

    // @AuthenticationPrincipal Long ownerId: Spring Security tu lay "principal" ma
    // JwtAuthenticationFilter (buoc 5) da ghi vao SecurityContext va bom thang vao tham so nay -
    // day chinh la Authentication that su thay the cho @RequestParam Long ownerId truoc day
    // (client tu xung la ai cung duoc). Gio ownerId luon la userId lay tu token da verify chu ky,
    // khong the gia mao.
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@AuthenticationPrincipal Long ownerId,
                                                    @Valid @RequestBody DeckRequest request) {
        DeckResponse response = deckService.createDeck(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> getMyDecks(@AuthenticationPrincipal Long ownerId) {
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
