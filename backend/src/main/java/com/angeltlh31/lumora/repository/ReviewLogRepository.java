package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    // Dung cho tinh streak: lay het cac NGAY (khong phai gio-phut-giay) khac nhau nguoi dung
    // nay tung on it nhat 1 the, sap xep MOI NHAT TRUOC (DESC). CAST(... AS date) can vi
    // reviewedAt la LocalDateTime (co gio-phut-giay) - 2 lan on cung 1 ngay nhung khac gio se
    // bi tinh la 2 dong KHAC NHAU neu khong CAST, lam sai lech ket qua DISTINCT.
    //
    // "ORDER BY 1 DESC" - so "1" o day nghia la "cot dau tien trong SELECT" (CAST(...) o tren),
    // dung vi bieu thuc CAST khong co ten cot de goi lai trong ORDER BY.
    @Query("SELECT DISTINCT CAST(r.reviewedAt AS date) FROM ReviewLog r " +
            "WHERE r.user.id = :userId ORDER BY 1 DESC")
    List<LocalDate> findDistinctReviewDatesByUserId(@Param("userId") Long userId);

    // Dung cho bieu do lich su: dem so lan DUNG va SAI theo TUNG NGAY, tinh tu 1 moc thoi
    // gian tro ve sau (vd 30 ngay gan nhat). Ket qua la List<Object[]> vi JPQL tra ve nhieu
    // cot "roi rac" (khong map thang vao 1 Entity/DTO nao) - moi phan tu Object[] co dung 3 o:
    // [0] = LocalDate (ngay), [1] = Boolean (correct), [2] = Long (so luong) - Service se tu
    // ep kieu (cast) lai tung o khi doc ket qua.
    @Query("SELECT CAST(r.reviewedAt AS date), r.correct, COUNT(r) FROM ReviewLog r " +
            "WHERE r.user.id = :userId AND r.reviewedAt >= :since " +
            "GROUP BY CAST(r.reviewedAt AS date), r.correct")
    List<Object[]> countGroupedByDateAndCorrect(@Param("userId") Long userId,
                                                 @Param("since") LocalDateTime since);

    // Dung cho "so lan on hom nay" - dem THEO LAN (attempt), khong phai theo the (card) khac
    // nhau, nen 1 the on lai nhieu lan trong ngay van cong don, dung tinh than "hoc bao nhieu
    // luot hom nay" hon la "hoc bao nhieu the hom nay".
    long countByUserIdAndReviewedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
