package com.angeltlh31.lumora.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Luu tien do on tap cua 1 User doi voi 1 Card cu the.
 * Moi cap (user, card) chi co dung 1 dong - dam bao bang unique constraint ben duoi.
 */
@Entity
@Table(
        name = "card_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    // He so "do de" cua SM-2: cang cao thi khoang cach den lan on tap sau cang gian nhanh.
    // Gia tri khoi tao chuan theo thuat toan goc la 2.5, khong bao gio duoi 1.3.
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private Double easeFactor = 2.5;

    // So ngay se doi den lan on tap tiep theo (interval).
    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private Integer intervalDays = 0;

    // So lan tra loi DUNG LIEN TIEP - reset ve 0 ngay khi tra loi sai 1 lan.
    @Column(nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.NEW;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
}
