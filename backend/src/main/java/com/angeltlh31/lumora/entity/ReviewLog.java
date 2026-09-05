package com.angeltlh31.lumora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Ngay 18: bang LOG - moi lan submitReview() thanh cong se INSERT THEM 1 dong moi o day,
 * KHONG BAO GIO update/xoa dong cu. Khac han CardProgress (moi cap user+card CHI CO DUNG 1
 * dong, lien tuc bi UPDATE de phan anh trang thai HIEN TAI). ReviewLog la "nhat ky" giu lai
 * TOAN BO lich su theo thoi gian - can thiet de tinh streak (chuoi ngay on lien tuc that su)
 * va ve bieu do lich su theo tuan/thang, hai thu ma CardProgress (chi luu 1 moc gan nhat)
 * KHONG the tra loi duoc.
 *
 * Danh doi: bang nay se PHINH TO dan theo thoi gian (khong gioi han so dong nhu CardProgress),
 * o quy mo du an hoc tap ca nhan thi khong dang lo - du an that se can them chinh sach xoa
 * bot du lieu cu (archive/partition), chua lam o day.
 */
@Entity
@Table(
        name = "review_log",
        indexes = @Index(name = "idx_review_log_user_reviewed_at", columnList = "user_id, reviewed_at")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false)
    private boolean correct;

    // Ngay dung de tinh streak/lich su - KHONG dung @Builder.Default vi luon duoc CardProgressService
    // gan ro rang bang LocalDateTime.now() ngay luc tao (giong lastReviewedAt cua CardProgress),
    // khong co truong hop nao can gia tri mac dinh khac.
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
}
