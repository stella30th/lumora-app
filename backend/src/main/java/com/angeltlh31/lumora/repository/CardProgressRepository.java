package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {

    // 1 cap (user, card) chi co dung 1 dong (xem unique constraint trong CardProgress.java) -
    // nen tra ve Optional thay vi List, giong tinh than findById cua JpaRepository.
    Optional<CardProgress> findByUserIdAndCardId(Long userId, Long cardId);

    // Ngay 12: dung cho CardProgressService.getDueCards - load progress cua NHIEU Card cung
    // luc trong 1 cau query duy nhat, thay vi goi findByUserIdAndCardId() lap lai trong vong
    // lap (N+1 query problem: N the can on se ra N+1 lan truy van database thay vi chi 2 lan).
    List<CardProgress> findByUserIdAndCardIdIn(Long userId, List<Long> cardIds);
}
