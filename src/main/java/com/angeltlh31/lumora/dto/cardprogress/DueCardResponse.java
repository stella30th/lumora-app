package com.angeltlh31.lumora.dto.cardprogress;

import com.angeltlh31.lumora.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// Ket hop thong tin tinh cua Card (term/definition) voi trang thai on tap dong (status/
// nextReviewDate) - Card moi toanh chua tung co CardProgress van xuat hien trong danh sach
// nay voi status=NEW, nextReviewDate=null (xem CardProgressService.toDueResponse: progress
// truyen vao co the la null dung cho truong hop nay).
@Getter
@Builder
public class DueCardResponse {
    private Long id;
    private String term;
    private String definition;
    private Long deckId;
    private ReviewStatus status;
    private LocalDate nextReviewDate;
}
