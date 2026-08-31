package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    // Giong findByOwnerId ben DeckRepository: -> SELECT * FROM cards WHERE deck_id = ?
    List<Card> findByDeckId(Long deckId);

    // Ngay 12: derived query method (findByXxx) KHONG the dien ta duoc dieu kien "HOAC chua
    // ton tai ban ghi CardProgress lien quan" - Spring Data JPA chi sinh duoc dieu kien tren
    // field/quan he co that, khong sinh duoc "OR not exists". Vi vay phai tu viet JPQL bang
    // @Query, dung LEFT JOIN (khac INNER JOIN mac dinh) de GIU LAI ca nhung Card khong khop
    // dieu kien join (cp la NULL) thay vi loai bo chung nhu INNER JOIN se lam.
    //
    // Day cung la lan dau tien join 2 entity KHONG co quan he @OneToMany/@ManyToOne khai bao
    // san giua Card va CardProgress (Card khong co field "List<CardProgress> progresses") -
    // JPQL cho phep join "khong theo duong quan he" nhu vay bang cach viet han dieu kien ON.
    //
    // Dieu kien loc user ("cp.user.id = :userId") duoc dat trong ON, KHONG phai WHERE: neu dat
    // o WHERE, dong CardProgress cua NGUOI KHAC (van khop LEFT JOIN vi thuoc ve dung Card do)
    // se bi loc mat truoc, lam sai lech ket qua - dung ra phai coi la "toi chua co progress",
    // khong phai "loai Card nay ra". Dat dieu kien nguoi dung TRONG ON moi dam bao LEFT JOIN
    // chi khop dung dong progress CUA NGUOI GOI, con moi Card khac deu giu lai voi cp = NULL.
    @Query("""
            SELECT c FROM Card c
            LEFT JOIN CardProgress cp ON cp.card = c AND cp.user.id = :userId
            WHERE c.deck.id = :deckId
              AND (cp IS NULL OR cp.nextReviewDate <= :today)
            """)
    List<Card> findDueCards(@Param("deckId") Long deckId,
                             @Param("userId") Long userId,
                             @Param("today") LocalDate today);
}
