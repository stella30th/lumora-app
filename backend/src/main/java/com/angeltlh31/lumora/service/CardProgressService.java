package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CardProgressService {

    // He so cong/tru EF va nguong "tot nghiep" tu LEARNING sang REVIEW - ban rut gon nhi phan
    // cua SM-2 (chi 2 muc dung/sai, tuong duong q=5/q=0 trong cong thuc goc). Xem
    // docs/recap-day12.md phan ly thuyet de biet vi sao chon dung cac hang so nay.
    private static final double EASE_BONUS_CORRECT = 0.1;
    private static final double EASE_PENALTY_WRONG = 0.8;
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final int GRADUATE_REPETITIONS = 2;

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CardProgressRepository cardProgressRepository;
    private final DeckAccessService deckAccessService;

    // Ngay 12: verifyReadAccess (KHONG phai verifyOwnership) - on tap chi can DOC duoc Deck
    // (chu so huu HOAC Deck public), giong het quyen xem Card (CardService.getCardsByDeck).
    // On mot Deck public cua nguoi khac van hop le, chi khong duoc SUA noi dung Card cua ho.
    public CardProgressResponse submitReview(Long cardId, Long userId, boolean correct) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id=" + cardId));
        deckAccessService.verifyReadAccess(card.getDeck(), userId);

        CardProgress progress = cardProgressRepository.findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> createNewProgress(userId, card));

        applyReview(progress, correct);
        return toResponse(cardProgressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public List<DueCardResponse> getDueCards(Long deckId, Long requesterId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id=" + deckId));
        deckAccessService.verifyReadAccess(deck, requesterId);

        List<Card> dueCards = cardRepository.findDueCards(deckId, requesterId, LocalDate.now());
        List<Long> cardIds = dueCards.stream().map(Card::getId).toList();

        // Load progress cua tat ca the trong 1 lan (tranh N+1) - key theo cardId de tra cuu
        // O(1) o buoc map ben duoi, thay vi loop tim tuyen tinh cho tung Card.
        Map<Long, CardProgress> progressByCardId = cardProgressRepository
                .findByUserIdAndCardIdIn(requesterId, cardIds)
                .stream()
                .collect(Collectors.toMap(cp -> cp.getCard().getId(), cp -> cp));

        return dueCards.stream()
                .map(card -> toDueResponse(card, progressByCardId.get(card.getId())))
                .toList();
    }

    // Ngay 12 - trai tim cua thuat toan spaced repetition (ban rut gon nhi phan cua SM-2).
    // Cong thuc goc dung thang chat luong q=0..5; o day chi con 2 nhanh dung/sai tuong duong
    // q=5 va q=0. Xem docs/recap-day12.md de doi chieu voi cong thuc SM-2 nguyen ban.
    //
    // Ngay 13: bo "private" -> chuyen thanh package-private (khong ghi modifier nao ca) DE
    // CardProgressServiceTest (cung package, khac thu muc src/test) co the goi thang method
    // nay ma khong can @SpringBootTest hay Mockito - vi day la logic thuan (khong dung DB,
    // khong dung repository nao trong 5 field cua class). Danh doi: giam encapsulation mot
    // chut (package khac van khong goi duoc, chi rieng test cung package moi goi duoc) de
    // doi lay kha nang test nhanh, khong can boot Spring context. Xem docs/recap-day13.md.
    void applyReview(CardProgress progress, boolean correct) {
        if (correct) {
            progress.setRepetitions(progress.getRepetitions() + 1);
            progress.setIntervalDays(nextInterval(progress));
            progress.setEaseFactor(progress.getEaseFactor() + EASE_BONUS_CORRECT);
            progress.setStatus(progress.getRepetitions() >= GRADUATE_REPETITIONS
                    ? ReviewStatus.REVIEW
                    : ReviewStatus.LEARNING);
        } else {
            progress.setRepetitions(0);
            progress.setIntervalDays(1);
            progress.setEaseFactor(Math.max(progress.getEaseFactor() - EASE_PENALTY_WRONG, MIN_EASE_FACTOR));
            progress.setStatus(ReviewStatus.LEARNING);
        }
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setNextReviewDate(LocalDate.now().plusDays(progress.getIntervalDays()));
    }

    // 3 buoc interval kinh dien cua SM-2: 1 ngay -> 6 ngay -> tu lan thu 3 tro di nhan don EF
    // (interval cu * easeFactor). Ham nay duoc goi SAU KHI repetitions da +1, nen
    // "repetitions == 1" nghia la "lan tra loi dung dau tien", khong phai "lan dau tien on".
    private int nextInterval(CardProgress progress) {
        return switch (progress.getRepetitions()) {
            case 1 -> 1;
            case 2 -> 6;
            default -> (int) Math.round(progress.getIntervalDays() * progress.getEaseFactor());
        };
    }

    private CardProgress createNewProgress(Long userId, Card card) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id=" + userId));
        // easeFactor/intervalDays/repetitions/status da co @Builder.Default trong entity
        // (2.5/0/0/NEW) - nextReviewDate se duoc applyReview() set NGAY SAU DAY, truoc khi
        // save(), nen khong vi pham rang buoc nullable=false cua cot next_review_date.
        return CardProgress.builder()
                .user(user)
                .card(card)
                .build();
    }

    private CardProgressResponse toResponse(CardProgress progress) {
        return CardProgressResponse.builder()
                .id(progress.getId())
                .cardId(progress.getCard().getId())
                .easeFactor(progress.getEaseFactor())
                .intervalDays(progress.getIntervalDays())
                .repetitions(progress.getRepetitions())
                .status(progress.getStatus())
                .nextReviewDate(progress.getNextReviewDate())
                .lastReviewedAt(progress.getLastReviewedAt())
                .build();
    }

    // progress co the null - nghia la Card nay chua tung duoc user ôn lan nao, chua co dong
    // CardProgress nao trong DB ca (findDueCards() van tra ve Card nay vi LEFT JOIN giu lai).
    private DueCardResponse toDueResponse(Card card, CardProgress progress) {
        return DueCardResponse.builder()
                .id(card.getId())
                .term(card.getTerm())
                .definition(card.getDefinition())
                .deckId(card.getDeck().getId())
                .status(progress != null ? progress.getStatus() : ReviewStatus.NEW)
                .nextReviewDate(progress != null ? progress.getNextReviewDate() : null)
                .build();
    }
}
