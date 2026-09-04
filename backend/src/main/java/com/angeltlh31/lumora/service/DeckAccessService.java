package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.exception.ForbiddenException;
import org.springframework.stereotype.Service;

// Ngay 12: tach ra tu DeckService/CardService - ca 2 noi da co 1 cap verifyOwnership/
// verifyReadAccess GIONG HET NHAU tu Ngay 9-10 (deu kiem tra quyen dua tren Deck cha, du
// "tai nguyen" truoc mat la Deck hay Card). Luc do da co comment ghi ro: lap lai 2 lan van
// chap nhan duoc (chua chac se co lan 3, tach som la "premature abstraction"), nhung gio
// CardProgressService la "khach hang" thu 3 can DUNG Y HET kieu kiem tra nay - day chinh la
// luc "rule of three" kich hoat: lap lai tu 3 lan tro len nen tach thanh 1 noi dung chung,
// neu khong, sua 1 quy tac phan quyen se phai nho sua o 3 (hoac nhieu hon) cho khac nhau,
// rat de sot mot cho.
@Service
public class DeckAccessService {

    // Quyen GHI: chi chu so huu THAT SU (deck.getOwner().getId(), doc tu database) moi duoc
    // thao tac. Dung cho: tao/sua/xoa Deck, tao/sua/xoa Card trong Deck.
    public void verifyOwnership(Deck deck, Long userId) {
        if (!deck.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify deck id=" + deck.getId());
        }
    }

    // Quyen DOC: cho phep khi Deck la public, HOAC nguoi goi la chu. Dung cho: xem Deck/Card,
    // va gio la nop ket qua on tap (submitReview) - on 1 Deck public cua nguoi khac la hop le,
    // chi khong duoc SUA noi dung Card cua ho (do van phai qua verifyOwnership).
    public void verifyReadAccess(Deck deck, Long requesterId) {
        boolean isOwner = deck.getOwner().getId().equals(requesterId);
        if (!deck.isPublic() && !isOwner) {
            throw new ForbiddenException(
                    "You do not have permission to view deck id=" + deck.getId());
        }
    }
}
