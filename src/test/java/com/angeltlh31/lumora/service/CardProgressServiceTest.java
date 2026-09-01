package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.ReviewStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Ngay 13 - test truc tiep applyReview(), KHONG dung @SpringBootTest, KHONG dung Mockito.
 *
 * Vi sao lam duoc dieu nay: applyReview() la "pure logic" - no chi doc/ghi len chinh doi
 * tuong CardProgress duoc truyen vao, khong goi ra ngoai (khong query DB, khong goi
 * repository/service nao trong 5 field cua CardProgressService). Vi vay chi can
 * "new CardProgressService(null, null, null, null, null)" la du de goi applyReview() -
 * 5 tham so do khong bao gio duoc dung toi trong ham nay nen truyen null hoan toan an toan.
 *
 * Vi sao goi truc tiep duoc tu file nay: applyReview() da doi tu "private" sang
 * package-private (Ngay 13, xem comment trong CardProgressService.java) - Maven/Gradle mac
 * dinh mirror cau truc package giua src/main va src/test, nen file nay cung nam trong
 * package "com.angeltlh31.lumora.service" va duoc phep goi thang, khong can reflection.
 *
 * Neu sau nay muon test submitReview() (method PUBLIC, co goi cardRepository.findById(),
 * deckAccessService.verifyReadAccess()...) thi BAT BUOC phai dung Mockito de mock 4 dependency
 * do - khac voi applyReview(), submitReview() co goi ra ngoai that su nen khong the chi
 * truyen null duoc nua. Day la viec de danh cho buoi sau.
 *
 * Bo so lieu ky vong lay dung tu bang "6 luot on" o docs/recap-day12.md (Phan 1, muc 6) -
 * mo file do song song de doi chieu tung dong so lieu voi tung assert ben duoi.
 */
class CardProgressServiceTest {

    // So thuc (double) trong Java khong bao gio nen so sanh bang "==" hay assertEquals thuong
    // - vi cach may tinh luu so thap phan co the lech mot chut o phan thap phan rat nho
    // (vd 2.5 + 0.1 co the luu thanh 2.6000000000000005 chu khong dung tuyet doi 2.6).
    // DELTA la "sai so cho phep" - assertEquals(expected, actual, DELTA) coi la bang neu
    // chenh lech nam trong khoang nay.
    private static final double DELTA = 0.0001;

    private CardProgressService service;

    @BeforeEach
    void setUp() {
        service = new CardProgressService(null, null, null, null, null);
    }

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
}
