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

// Giong CardController - khong dat @RequestMapping o class vi 2 nhom route khac goc:
// /api/cards/{cardId}/review (thao tac tren 1 Card) va /api/decks/{deckId}/review-queue
// (truy van theo Deck cha, cung kieu long route nhu /api/decks/{deckId}/cards).
@RestController
@RequiredArgsConstructor
public class CardProgressController {

    private final CardProgressService cardProgressService;

    @Operation(summary = "Nop ket qua on 1 Card (dung/sai) - cap nhat lich on tiep theo theo thuat toan spaced repetition")
    @PostMapping("/api/cards/{cardId}/review")
    public ResponseEntity<CardProgressResponse> submitReview(@PathVariable Long cardId,
                                                               @AuthenticationPrincipal Long userId,
                                                               @Valid @RequestBody CardReviewRequest request) {
        return ResponseEntity.ok(cardProgressService.submitReview(cardId, userId, request.isCorrect()));
    }

    @Operation(summary = "Lay danh sach Card can on hom nay trong 1 Deck (gom ca the chua tung on lan nao)")
    @GetMapping("/api/decks/{deckId}/review-queue")
    public ResponseEntity<List<DueCardResponse>> getDueCards(@PathVariable Long deckId,
                                                               @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(cardProgressService.getDueCards(deckId, requesterId));
    }
}
