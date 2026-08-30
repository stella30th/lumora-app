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

    // @AuthenticationPrincipal Long ownerId: Spring Security tu lay "principal" ma
    // JwtAuthenticationFilter (buoc 5) da ghi vao SecurityContext va bom thang vao tham so nay -
    // day chinh la Authentication that su thay the cho @RequestParam Long ownerId truoc day
    // (client tu xung la ai cung duoc). Gio ownerId luon la userId lay tu token da verify chu ky,
    // khong the gia mao.
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

    // Ngay 11: route rieng cho Deck cong khai - KHONG dung @PathVariable Long id vi "public"
    // khong phai 1 id. Spring tu phan biet duoc "/api/decks/public" (literal) voi
    // "/api/decks/{id}" (bien) du khai bao method nao truoc - PathPattern cua Spring MVC luon
    // uu tien khop chinh xac (literal segment) truoc khi thu khop bien, bat ke thu tu code.
    // Khong nhan @AuthenticationPrincipal vi khong can biet nguoi goi la ai - moi Deck tra ve
    // deu isPublic = true nen khong co gi de kiem tra quyen so huu ca.
    @Operation(summary = "Lay danh sach Deck cong khai (khong loc theo chu so huu)")
    @GetMapping("/public")
    public ResponseEntity<List<DeckResponse>> getPublicDecks() {
        return ResponseEntity.ok(deckService.getPublicDecks());
    }

    // Ngay 10: them @AuthenticationPrincipal Long requesterId - dat ten "requesterId" (khong
    // phai "ownerId") vi nguoi goi CHUA CHAC la chu; Service se tu quyet dinh co cho xem hay
    // khong dua vao isPublic cua Deck.
    @Operation(summary = "Xem chi tiet 1 Deck (yeu cau Deck public hoac la chu so huu)")
    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id,
                                                     @AuthenticationPrincipal Long requesterId) {
        return ResponseEntity.ok(deckService.getDeckById(id, requesterId));
    }

    // Ngay 9: them @AuthenticationPrincipal Long ownerId - KHONG dung de tao du lieu (nhu
    // createDeck) ma de DeckService so sanh voi chu so huu that su cua Deck id nay. Neu khong
    // khop, Service nem ForbiddenException -> GlobalExceptionHandler tra 403.
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
