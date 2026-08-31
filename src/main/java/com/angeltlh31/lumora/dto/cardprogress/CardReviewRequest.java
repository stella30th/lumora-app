package com.angeltlh31.lumora.dto.cardprogress;

import lombok.Getter;
import lombok.Setter;

// Nhi phan Dung/Sai (khong dung thang 0-5 cua SM-2 goc) - xem docs/recap-day12.md phan ly
// thuyet de biet ly do chon huong nay. "correct" la field don gian nhat de danh gia 1 lan
// on 1 Card, tuong duong q=5 (dung) / q=0 (sai) trong cong thuc goc.
@Getter
@Setter
public class CardReviewRequest {
    private boolean correct;
}
