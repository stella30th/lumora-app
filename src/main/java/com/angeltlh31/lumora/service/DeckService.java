package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.deck.DeckRequest;
import com.angeltlh31.lumora.dto.deck.DeckResponse;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ForbiddenException;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeckService {

    private final DeckRepository deckRepository; //DI
    private final UserRepository userRepository;

    public DeckResponse createDeck(Long ownerId, DeckRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay User id=" + ownerId));

        Deck deck = Deck.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .owner(owner)
                .build();

        return toResponse(deckRepository.save(deck));
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> getDecksByOwner(Long ownerId) {
        return deckRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Ngay 11: khac getDecksByOwner o cho KHONG loc theo ownerId nao ca - tra ve TAT CA Deck
    // co is_public = true, bat ke ai la chu. Khong can requesterId/verifyReadAccess vi ban than
    // dieu kien isPublic = true da la "ai xem cung duoc", khong con gi de kiem tra them.
    @Transactional(readOnly = true)
    public List<DeckResponse> getPublicDecks() {
        return deckRepository.findByIsPublicTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Ngay 10: doc (read) KHONG dung logic "chi chu moi duoc" nhu ghi (write). Deck co san cot
    // isPublic - nguoi goi duoc xem neu: deck la public, HOAC nguoi goi chinh la chu. Vi vay
    // nhan them requesterId (tu JWT, giong ownerId o cac method ghi) va dung verifyReadAccess
    // (KHAC verifyOwnership) de kiem tra.
    @Transactional(readOnly = true)
    public DeckResponse getDeckById(Long id, Long requesterId) {
        Deck deck = findDeckOrThrow(id);
        verifyReadAccess(deck, requesterId);
        return toResponse(deck);
    }

    // Ngay 9: nhan them ownerId (lay tu token qua Controller), KHONG con tin tuong tuyet doi
    // vao PathVariable id nua. verifyOwnership nem ForbiddenException truoc khi kip sua gi ca
    // neu id nay khong phai Deck cua ownerId dang goi.
    public DeckResponse updateDeck(Long id, Long ownerId, DeckRequest request) {
        Deck deck = findDeckOrThrow(id);
        verifyOwnership(deck, ownerId);

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setPublic(request.isPublic());
        // Khong goi deckRepository.save(deck) - xem giai thich "dirty checking" ben duoi.
        return toResponse(deck);
    }

    public void deleteDeck(Long id, Long ownerId) {
        Deck deck = findDeckOrThrow(id);
        verifyOwnership(deck, ownerId);
        deckRepository.delete(deck);
    }

    private Deck findDeckOrThrow(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay Deck id=" + id));
    }

    // Tach rieng 1 method dung chung cho ca updateDeck/deleteDeck: so sanh chu so huu THAT
    // (deck.getOwner().getId(), doc tu database) voi nguoi dang goi (ownerId, lay tu JWT da
    // verify) - khong khop nghia la dung Deck ton tai that, nhung khong phai cua nguoi nay.
    private void verifyOwnership(Deck deck, Long ownerId) {
        if (!deck.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException(
                    "Ban khong co quyen thao tac tren Deck id=" + deck.getId());
        }
    }

    // Ngay 10: quyen DOC - khac verifyOwnership (chi chu moi qua duoc, dung cho GHI).
    // Cho phep khi Deck la public, HOAC nguoi goi la chu. Tach rieng ham nay thay vi sua
    // verifyOwnership vi 2 ham mang ngu nghia nghiep vu khac nhau (owner-only vs owner-OR-public)
    // - gop chung se phai them tham so co/khong check public, doc kho hieu hon la tach ro.
    private void verifyReadAccess(Deck deck, Long requesterId) {
        boolean isOwner = deck.getOwner().getId().equals(requesterId);
        if (!deck.isPublic() && !isOwner) {
            throw new ForbiddenException(
                    "Ban khong co quyen xem Deck id=" + deck.getId());
        }
    }

    private DeckResponse toResponse(Deck deck) {
        return DeckResponse.builder()
                .id(deck.getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .isPublic(deck.isPublic())
                .ownerId(deck.getOwner().getId())
                .createdAt(deck.getCreatedAt())
                .build();
    }
}
