package com.angeltlh31.lumora.controller;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.CardReviewRequest;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.service.CardProgressService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardProgressController {
    private final CardProgressService cardProgressService;

    @Operation(summary = "Nop ket qua on 1 Card (AGAIN/HARD/GOOD/EASY) - cap nhat lich on tiep theo theo thuat toan SM-2")
    @PostMapping("/api/cards/{cardId}/review")
    public ResponseEntity<CardProgressResponse> submitReview(@PathVariable Long cardId,
                                                               @AuthenticationPrincipal Long userId,
                                                               @Valid @RequestBody CardReviewRequest request) {
        return ResponseEntity.ok(cardProgressService.submitReview(cardId, userId, request.getRating()));
    }

    @Operation(summary = "Lay hang doi on hom nay cua 1 Deck: moi the den han + toi da N the moi/ngay")
    @GetMapping("/api/decks/{deckId}/review-queue")
    public ResponseEntity<List<DueCardResponse>> getDueCards(@PathVariable Long deckId,
                                                               @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardProgressService.getDueCards(deckId, requesterId));
    }
}
