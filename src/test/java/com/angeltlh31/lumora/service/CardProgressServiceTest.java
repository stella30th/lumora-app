package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.dto.cardprogress.CardProgressResponse;
import com.angeltlh31.lumora.dto.cardprogress.DueCardResponse;
import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import com.angeltlh31.lumora.exception.ForbiddenException;
import com.angeltlh31.lumora.exception.ResourceNotFoundException;
import com.angeltlh31.lumora.repository.CardProgressRepository;
import com.angeltlh31.lumora.repository.CardRepository;
import com.angeltlh31.lumora.repository.DeckRepository;
import com.angeltlh31.lumora.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/*
 * Ngay 13 test truc tiep applyReview() (pure logic, khong dung ra ngoai) bang cach truyen
 * null cho ca 5 dependency. Ngay 14 test them submitReview() va getDueCards() - 2 method
 * PUBLIC co goi that ra ngoai (cardRepository, deckRepository, userRepository,
 * cardProgressRepository, deckAccessService) nen KHONG the truyen null duoc nua: goi 1
 * method tren gia tri null se nem NullPointerException ngay lap tuc. Day la luc BAT BUOC
 * phai dung Mockito de tao "vat gia" (mock/test double) cho ca 5 dependency do.
 *
 * 3 khai niem Mockito cot loi dung trong file nay:
 *   - @Mock: tao 1 doi tuong GIA cho 1 interface/class (vd CardRepository). Doi tuong nay
 *     khong noi voi database that - moi method cua no mac dinh khong lam gi ca (method tra
 *     ve gia tri se tra ve null/0/false) CHO TOI KHI ta "day loi" cho no bang when(...).
 *   - @InjectMocks: bao Mockito "hay tu tao 1 CardProgressService that, va TU DONG truyen
 *     nhung @Mock ben tren vao dung constructor cua no" - thay cho viec ta tu tay goi
 *     "new CardProgressService(m1, m2, m3, m4, m5)" nhu Ngay 13.
 *   - @ExtendWith(MockitoExtension.class): "cong tac tong" bat Mockito hoat dong trong file
 *     JUnit 5 nay - neu thieu dong nay, cac @Mock/@InjectMocks o tren se khong duoc khoi tao,
 *     tat ca deu la null va test se nem NullPointerException ngay dong dau tien.
 *
 * MockitoExtension tu dong tao LAI moi @Mock/@InjectMocks TRUOC MOI method @Test (giong
 * @BeforeEach nhung Mockito lam gium) - vi vay khong con can vong "@BeforeEach { service =
 * new CardProgressService(...) }" thu cong nhu Ngay 13 nua, da xoa di. 2 test cua Ngay 13 o
 * duoi van chay dung y het - applyReview() van khong dung toi field nao trong 5 mock, nen
 * viec 5 field do gio la mock-rong (thay vi null) khong lam thay doi ket qua gi ca.
 *
 * Xem docs/recap-day14.md de doi chieu ly thuyet chi tiet (when/thenReturn, verify,
 * doThrow, ArgumentCaptor) voi tung test ben duoi.
 */
@ExtendWith(MockitoExtension.class)
class CardProgressServiceTest {

    private static final double DELTA = 0.0001;

    @Mock
    private CardRepository cardRepository;
    @Mock
    private DeckRepository deckRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardProgressRepository cardProgressRepository;
    @Mock
    private DeckAccessService deckAccessService;

    @InjectMocks
    private CardProgressService service;

    // ============================================================
    // Ngay 13 - applyReview(): pure logic, khong stub mock nao ca (khong dung toi)
    // ============================================================

    @Test
    void spacedRepetitionSixRoundSequence_matchesDesignTable() {
        // CardProgress.builder().build() ap dung dung @Builder.Default cua entity:
        // easeFactor=2.5, intervalDays=0, repetitions=0, status=NEW - giong het 1 the moi toanh.
        CardProgress progress = CardProgress.builder().build();

        // Luot 1 (Day 0) - Dung: "tot nghiep" buoc 1, interval nhay thang len 1 (co dinh)
        service.applyReview(progress, true);
        assertAll("Luot 1",
                () -> assertEquals(1, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.6, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus()),
                () -> assertEquals(LocalDate.now().plusDays(1), progress.getNextReviewDate())
        );

        // Luot 2 (Day 1) - Dung: "tot nghiep" buoc 2 (repetitions >= 2 -> status REVIEW),
        // interval nhay thang len 6 (co dinh, chua nhan EF)
        service.applyReview(progress, true);
        assertAll("Luot 2",
                () -> assertEquals(2, progress.getRepetitions().intValue()),
                () -> assertEquals(6, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.7, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );

        // Luot 3 (Day 7) - Dung: tu day interval = round(interval_cu * easeFactor_cu)
        // = round(6 * 2.7) = round(16.2) = 16
        service.applyReview(progress, true);
        assertAll("Luot 3",
                () -> assertEquals(3, progress.getRepetitions().intValue()),
                () -> assertEquals(16, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.8, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );

        // Luot 4 (Day 23) - SAI: repetitions va intervalDays reset ve 0/1, NHUNG easeFactor
        // chi GIAM (2.8 - 0.8 = 2.0), khong reset ve 2.5 mac dinh - day la diem de hieu nham nhat.
        service.applyReview(progress, false);
        assertAll("Luot 4",
                () -> assertEquals(0, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.0, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus())
        );

        // Luot 5 (Day 24) - Dung: hoc lai tu dau, nhung easeFactor xuat phat tu 2.0 (khong
        // phai 2.5 nhu the moi hoan toan) - "lich su" truoc do van con anh huong.
        service.applyReview(progress, true);
        assertAll("Luot 5",
                () -> assertEquals(1, progress.getRepetitions().intValue()),
                () -> assertEquals(1, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.1, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, progress.getStatus())
        );

        // Luot 6 (Day 25) - Dung: "tot nghiep" lan 2 (lai), interval nhay thang len 6
        service.applyReview(progress, true);
        assertAll("Luot 6",
                () -> assertEquals(2, progress.getRepetitions().intValue()),
                () -> assertEquals(6, progress.getIntervalDays().intValue()),
                () -> assertEquals(2.2, progress.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.REVIEW, progress.getStatus())
        );
    }

    @Test
    void easeFactor_neverGoesBelowMinimum_evenAfterManyWrongAnswersInARow() {
        // Bat dau tu mot the da kha "kho" (easeFactor gan sat nguong 1.3) de kiem tra
        // Math.max(..., MIN_EASE_FACTOR) trong applyReview() co chan dung nhu thiet ke khong.
        CardProgress progress = CardProgress.builder().easeFactor(1.5).build();

        service.applyReview(progress, false); // 1.5 - 0.8 = 0.7 -> bi chan lai o 1.3
        assertEquals(1.3, progress.getEaseFactor(), DELTA);

        service.applyReview(progress, false); // 1.3 - 0.8 = 0.5 -> van bi chan o 1.3
        assertEquals(1.3, progress.getEaseFactor(), DELTA);

        service.applyReview(progress, false); // lap lai bao nhieu lan cung khong xuong duoi 1.3
        assertEquals(1.3, progress.getEaseFactor(), DELTA);
    }

    // ============================================================
    // Ngay 14 - submitReview(): co goi ra ngoai that -> phai stub mock bang when(...)
    // ============================================================

    @Test
    void submitReview_existingProgress_correctAnswer_updatesAndSaves() {
        Long cardId = 10L;
        Long userId = 1L;
        User owner = User.builder().id(userId).build();
        Deck deck = Deck.builder().id(2L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();
        // Da co progress tu truoc (repetitions=0, easeFactor=2.5 - giong the moi) - dung de
        // phan biet voi test ben duoi (chua co progress, phai TAO MOI).
        CardProgress existingProgress = CardProgress.builder()
                .id(99L)
                .user(owner)
                .card(card)
                .easeFactor(2.5)
                .intervalDays(0)
                .repetitions(0)
                .status(ReviewStatus.NEW)
                .build();

        // when(mock.method(...)).thenReturn(...): "day loi" cho mock - moi khi code that su
        // goi cardRepository.findById(10L), tra ve Optional.of(card) thay vi di hoi database
        // that (that ra khong co database nao trong unit test ca).
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardProgressRepository.findByUserIdAndCardId(userId, cardId))
                .thenReturn(Optional.of(existingProgress));
        // thenAnswer thay vi thenReturn co dinh: cardProgressRepository.save(x) trong doi song
        // that se tra ve DUNG doi tuong da luu (co the da duoc JPA gan them id) - o day ta gia
        // lap dung hanh vi do bang cach tra ve CHINH tham so vua duoc truyen vao save().
        when(cardProgressRepository.save(any(CardProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardProgressResponse response = service.submitReview(cardId, userId, true);

        assertAll("Response phan anh dung applyReview() cho 1 lan tra loi DUNG dau tien",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(1, response.getIntervalDays()),
                () -> assertEquals(2.6, response.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, response.getStatus()),
                () -> assertEquals(LocalDate.now().plusDays(1), response.getNextReviewDate())
        );

        // verify(mock).method(...): xac nhan method NAY THAT SU DA duoc goi, dung 1 lan, voi
        // dung tham so nay - khac han when(...) (day du lieu VAO), verify(...) kiem tra HANH
        // VI da xay ra sau khi goi service, giong nhu "coi lai lich su cuoc goi" cua mock.
        verify(deckAccessService).verifyReadAccess(deck, userId);
        verify(cardProgressRepository).save(existingProgress);
        // Da co san progress (findByUserIdAndCardId tra ve non-empty) nen nhanh createNewProgress()
        // KHONG duoc chay - never() xac nhan 1 method KHONG bao gio duoc goi trong test nay.
        verify(userRepository, never()).findById(any());
    }

    @Test
    void submitReview_noExistingProgress_createsNewProgressFromUser() {
        Long cardId = 20L;
        Long userId = 1L;
        User owner = User.builder().id(userId).build();
        Deck deck = Deck.builder().id(3L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        // Optional.empty() = "chua tung on the nay" -> submitReview() phai re vao nhanh
        // createNewProgress(), nhanh nay can goi them userRepository.findById().
        when(cardProgressRepository.findByUserIdAndCardId(userId, cardId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(cardProgressRepository.save(any(CardProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardProgressResponse response = service.submitReview(cardId, userId, true);

        assertAll("The moi toanh, tra loi DUNG lan dau - giong het 'Luot 1' o test applyReview()",
                () -> assertEquals(1, response.getRepetitions()),
                () -> assertEquals(2.6, response.getEaseFactor(), DELTA),
                () -> assertEquals(ReviewStatus.LEARNING, response.getStatus())
        );

        // ArgumentCaptor: "bat" lai CHINH doi tuong CardProgress ma code that su truyen vao
        // save(...), de kiem tra CHI TIET ben trong no (user/card co duoc gan dung khong).
        // verify(...) thuong (nhu test tren) chi cho biet "co goi hay khong", khong cho xem
        // duoc NOI DUNG tham so - can capture() khi muon soi ky tham so đo.
        ArgumentCaptor<CardProgress> savedCaptor = ArgumentCaptor.forClass(CardProgress.class);
        verify(cardProgressRepository).save(savedCaptor.capture());
        assertEquals(owner, savedCaptor.getValue().getUser());
        assertEquals(card, savedCaptor.getValue().getCard());
        verify(userRepository).findById(userId); // xac nhan createNewProgress() THAT SU da chay
    }

    @Test
    void submitReview_cardNotFound_throwsResourceNotFoundException() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // assertThrows tra ve chinh exception bat duoc - o day khong can doc message nen bo qua,
        // chi can biet DUNG loai exception (ResourceNotFoundException) da bi nem ra.
        assertThrows(ResourceNotFoundException.class,
                () -> service.submitReview(cardId, 1L, true));

        // Card khong ton tai -> code phai "chet som" (fail fast) tai dong findById(), khong duoc
        // di tiep kiem tra quyen hay dung toi CardProgress nao ca. verifyNoInteractions kiem tra
        // CA MOT nhom mock hoan toan chua bi goi method nao - gon hon viet nhieu dong never().
        verifyNoInteractions(deckAccessService, cardProgressRepository);
    }

    @Test
    void submitReview_noReadAccess_throwsForbiddenException() {
        Long cardId = 30L;
        Long userId = 2L; // khong phai chu Deck, va Deck khong public
        User owner = User.builder().id(1L).build();
        Deck deck = Deck.builder().id(4L).owner(owner).isPublic(false).build();
        Card card = Card.builder().id(cardId).deck(deck).build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        // doThrow(...).when(mock).voidMethod(...): CACH BAT BUOC de gia lap 1 method tra ve
        // void nem ra exception. Khac voi when(mock.method()).thenThrow(...) - cach do chi
        // dung duoc khi method CO gia tri tra ve (vi when() can goi method that de "ghi lai"
        // loi goi do, nhung verifyReadAccess() la void nen khong the dung lam tham so cho
        // when() duoc - phai dao nguoc thu tu thanh doThrow().when(...) nhu the nay.
        doThrow(new ForbiddenException("Khong co quyen xem Deck id=" + deck.getId()))
                .when(deckAccessService).verifyReadAccess(deck, userId);

        assertThrows(ForbiddenException.class,
                () -> service.submitReview(cardId, userId, true));

        // Bi chan quyen ngay o verifyReadAccess() -> khong duoc di toi buoc tim/tao CardProgress.
        verifyNoInteractions(cardProgressRepository);
    }

    // ============================================================
    // Ngay 14 - getDueCards(): chu y nhanh "progress == null" (the chua tung on lan nao)
    // ============================================================

    @Test
    void getDueCards_mixesCardsWithAndWithoutExistingProgress() {
        Long deckId = 5L;
        Long requesterId = 1L;
        User owner = User.builder().id(requesterId).build();
        Deck deck = Deck.builder().id(deckId).owner(owner).isPublic(false).build();
        Card cardWithProgress = Card.builder().id(100L).deck(deck)
                .term("apple").definition("qua tao").build();
        Card cardWithoutProgress = Card.builder().id(101L).deck(deck)
                .term("banana").definition("qua chuoi").build();
        CardProgress progress = CardProgress.builder()
                .card(cardWithProgress)
                .status(ReviewStatus.REVIEW)
                .nextReviewDate(LocalDate.now())
                .build();

        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));
        // eq(...)/any(...): khi mot method co NHIEU tham so va ta chi can khop CHINH XAC 1 vai
        // tham so (deckId, requesterId), con tham so con lai (LocalDate.now() - gan nhu khong
        // bao gio khop tuyet doi 2 lan goi khac nhau vi thoi gian troi qua) chi can "bat ky
        // gia tri nao cung kieu LocalDate" - PHAI dung eq()/any() cho MOI tham so 1 khi da
        // dung 1 matcher trong cung 1 loi goi (Mockito bat buoc quy tac nay).
        when(cardRepository.findDueCards(eq(deckId), eq(requesterId), any(LocalDate.class)))
                .thenReturn(List.of(cardWithProgress, cardWithoutProgress));
        // Chi tra ve progress cua card 100 - mo phong dung tinh huong card 101 CHUA TUNG duoc
        // on lan nao (khong co dong CardProgress nao trong DB that).
        when(cardProgressRepository.findByUserIdAndCardIdIn(eq(requesterId), anyList()))
                .thenReturn(List.of(progress));

        List<DueCardResponse> result = service.getDueCards(deckId, requesterId);

        assertEquals(2, result.size());
        DueCardResponse withProgress = result.stream()
                .filter(r -> r.getId().equals(100L)).findFirst().orElseThrow();
        DueCardResponse withoutProgress = result.stream()
                .filter(r -> r.getId().equals(101L)).findFirst().orElseThrow();

        assertAll("The DA co CardProgress -> lay status/nextReviewDate tu progress that",
                () -> assertEquals(ReviewStatus.REVIEW, withProgress.getStatus()),
                () -> assertEquals(LocalDate.now(), withProgress.getNextReviewDate())
        );
        // Day chinh la nhanh "progress == null" trong toDueResponse() (CardProgressService) -
        // the chua co dong CardProgress nao van phai hien thi duoc, voi gia tri mac dinh NEW.
        assertAll("The CHUA TUNG on (progress == null trong toDueResponse) -> mac dinh NEW",
                () -> assertEquals(ReviewStatus.NEW, withoutProgress.getStatus()),
                () -> assertNull(withoutProgress.getNextReviewDate())
        );
    }

    @Test
    void getDueCards_deckNotFound_throwsResourceNotFoundException() {
        Long deckId = 6L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getDueCards(deckId, 1L));

        // Deck khong ton tai -> "chet som" ngay tai findById(), khong duoc di tiep toi
        // buoc kiem tra quyen hay truy van Card/CardProgress nao ca.
        verifyNoInteractions(cardRepository, cardProgressRepository, deckAccessService);
    }
}
