package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import com.angeltlh31.lumora.entity.CardProgress;
import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.entity.ReviewStatus;
import com.angeltlh31.lumora.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Ngay 16 - INTEGRATION TEST dau tien cua du an, khac hoan toan ban chat voi UNIT TEST
 * (Ngay 13-15). Doc ky truoc khi doc tung test ben duoi - xem them docs/recap-day16.md phan
 * ly thuyet day du (unit vs integration, test double vs dependency that, vi sao khong dung H2).
 *
 * Tom tat khac biet cot loi:
 * - Unit test (CardProgressServiceTest): thay CardRepository/DeckAccessService/... bang
 *   MOCK (Mockito) - ta TU KHAI BAO "khi goi X thi tra ve Y", khong co Postgres nao chay ca.
 *   Nhanh (mili-giay), nhung KHONG the biet cau @Query JPQL that su co dung khong - mock chi
 *   "dien vai" repository, khong thuc su chay SQL nao.
 * - Integration test (file nay): dung THAT Spring Data JPA + THAT Postgres (qua Testcontainers -
 *   xem duoi) - findDueCards() duoc goi THAT, JPQL duoc Hibernate dich thanh SQL THAT, chay
 *   tren Postgres THAT roi tra ket qua that ve. Cham hon (vai giay, phai khoi Docker container),
 *   nhung day la cach DUY NHAT xac nhan cau JOIN...ON phuc tap trong CardRepository.findDueCards()
 *   (xem lai comment Ngay 12 trong chinh CardRepository.java) thuc su dung, khong chi "co ve dung"
 *   khi doc bang mat.
 *
 * Ten class ket thuc bang "IT" (khong phai "Test") CO CHU DICH - xem pom.xml: maven-failsafe-plugin
 * chi quet file *IT.java, chay o buoc `mvn verify` (rieng khoi `mvn test` cua surefire-plugin,
 * la buoc chi quet *Test.java). Tach nhu vay de `./mvnw test` (dung hang ngay, chay lai lien tuc)
 * luon NHANH va KHONG can Docker - chi `./mvnw verify` (chay truoc khi merge/deploy) moi can Docker
 * dang mo va cham hon vi phai khoi container that.
 */
@DataJpaTest
// GOTCHA quan trong nhat cua bai hoc nay: @DataJpaTest MAC DINH tu thay DataSource cau hinh
// that (Postgres o application.properties) bang 1 database H2 nhung tam trong bo nho - NEU
// khong tat hanh vi nay, du co khai bao PostgreSQLContainer ben duoi, test VAN CHAY tren H2
// chu khong phai Postgres that (Testcontainers khoi container nhung khong ai dung toi no ca).
// Replace.NONE = "dung DUNG datasource ma @ServiceConnection ben duoi cung cap, dung tu thay gi ca".
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// "Cong tac tong" cua Testcontainers cho JUnit 5 - kich hoat vong doi tu dong cho moi field
// duoc danh dau @Container ben duoi (tu khoi container TRUOC khi test chay, tu dong tat/xoa
// container SAU KHI het test - giong tinh than MockitoExtension o Ngay 14 nhung cho Docker
// container thay vi mock object).
@Testcontainers
class CardRepositoryIT {

    // static: CHI 1 container duoc khoi cho CA CLASS (moi lan chay ca file test), khong phai
    // 1 container rieng cho MOI method @Test - khoi container mat vai giay, lam vay cho MOI
    // test se rat cham. Du dung chung 1 container, MOI test van CACH LY du lieu voi nhau nho
    // co che rollback transaction cua @DataJpaTest (xem comment o setUp()).
    //
    // @ServiceConnection: Spring Boot 4 tu doc container nay (thay "postgresql" tu image name),
    // tu tao cac bean JdbcConnectionDetails tro dung ve URL/user/password THAT SU cua container
    // (cong ngau nhien Docker gan luc chay, khong phai cong 5434 co dinh trong application.properties)
    // - khong can tu tay ghi @DynamicPropertySource nhu cach lam thu cong truoc day.
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // TestEntityManager: ban "than thien voi test" cua EntityManager (JPA) chuan - co
    // persistAndFlush() ghi THANG xuong DB va FLUSH NGAY (khong doi transaction commit tu
    // nhien), can thiet o day vi test can du lieu "co that" trong Postgres TRUOC KHI goi
    // cardRepository.findDueCards() (goi 1 method repository khac hoan toan, khong tu thay
    // duoc du lieu vua persist() binh thuong neu chua flush).
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    private User owner;
    private Deck deck;

    // Moi method @Test cua @DataJpaTest MAC DINH chay trong 1 transaction rieng, TU DONG
    // ROLLBACK sau khi test xong - vi vay khong can tu tay xoa du lieu giua cac test (khac
    // han moi lo XOA/DON DEP thu cong o database that). setUp() nay tao lai owner/deck moi
    // toanh cho MOI test, khong test nao "nhin thay" du lieu cua test khac.
    @BeforeEach
    void setUp() {
        owner = persistUser("owner", "owner@lumora.test");
        deck = persistDeck(owner, "Tieng Anh co ban");
    }

    @Test
    void findDueCards_includesCardWithNoProgressYet() {
        Card card = persistCard(deck, "apple", "qua tao");
        // Khong tao CardProgress nao ca - dung Ngay 12 mo ta: the CHUA TUNG on van phai
        // duoc coi la "den han" (LEFT JOIN giu lai dong nay voi cp = NULL).

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    @Test
    void findDueCards_includesCardWhoseNextReviewDateIsTodayOrEarlier() {
        Card card = persistCard(deck, "apple", "qua tao");
        persistProgress(owner, card, LocalDate.now().minusDays(1)); // qua han 1 ngay

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    @Test
    void findDueCards_excludesCardWhoseNextReviewDateIsInTheFuture() {
        Card card = persistCard(deck, "apple", "qua tao");
        persistProgress(owner, card, LocalDate.now().plusDays(5)); // con lau moi den han

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertTrue(result.isEmpty());
    }

    @Test
    void findDueCards_excludesCardFromOtherDeck() {
        Deck otherDeck = persistDeck(owner, "Deck khac");
        persistCard(otherDeck, "banana", "qua chuoi"); // thuoc Deck KHAC, khong phai "deck" cua setUp()

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        assertTrue(result.isEmpty());
    }

    // ĐÂY LÀ TEST QUAN TRỌNG NHẤT FILE NÀY - xac nhan dung diem de sai nhat trong JPQL cua
    // findDueCards() (xem comment Ngay 12 trong CardRepository.java): dieu kien loc user PHAI
    // nam trong ON, KHONG duoc nam trong WHERE.
    @Test
    void findDueCards_otherUsersProgressDoesNotHideCardFromThisUser() {
        User otherUser = persistUser("other", "other@lumora.test");
        Card card = persistCard(deck, "apple", "qua tao");
        // otherUser DA on the nay roi va con lau moi den han lai (tuong lai xa) - "owner"
        // (nguoi goi that su cua test) thi CHUA TUNG on the nay lan nao ca.
        persistProgress(otherUser, card, LocalDate.now().plusDays(30));

        List<Card> result = cardRepository.findDueCards(deck.getId(), owner.getId(), LocalDate.now());

        // Neu ai do "don gian hoa" JPQL bang cach doi "cp.user.id = :userId" tu ON sang WHERE
        // (nhin qua tuong tuong duong, viet code sai ma van compile binh thuong), dong
        // CardProgress cua otherUser se bi WHERE loc mat TRUOC, Card nay bien mat khoi ket qua
        // hoan toan - SAI, vi voi rieng "owner", the nay VAN la the chua tung on, phai duoc
        // coi la den han. Test nay se BAO DO (failed) ngay neu ai vo tinh doi nham ON -> WHERE,
        // trong khi unit test (Mockito) khong bao gio phat hien duoc loi nay - Mockito khong
        // biet gi ve ON/WHERE ca, no chi tra ve dung cai ta bao no tra ve.
        assertEquals(1, result.size());
        assertEquals(card.getId(), result.get(0).getId());
    }

    private User persistUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("khong-quan-trong-trong-test-nay")
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Deck persistDeck(User owner, String name) {
        Deck deck = Deck.builder()
                .name(name)
                .owner(owner)
                .isPublic(false)
                .build();
        return entityManager.persistAndFlush(deck);
    }

    private Card persistCard(Deck deck, String term, String definition) {
        Card card = Card.builder()
                .deck(deck)
                .term(term)
                .definition(definition)
                .build();
        return entityManager.persistAndFlush(card);
    }

    private CardProgress persistProgress(User user, Card card, LocalDate nextReviewDate) {
        CardProgress progress = CardProgress.builder()
                .user(user)
                .card(card)
                .status(ReviewStatus.LEARNING)
                .nextReviewDate(nextReviewDate) // KHONG co @Builder.Default - bat buoc tu set
                .build();
        return entityManager.persistAndFlush(progress);
    }
}
