package com.angeltlh31.lumora.repository;

import com.angeltlh31.lumora.entity.CardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardProgressRepository extends JpaRepository<CardProgress, Long> {

    Optional<CardProgress> findByUserIdAndCardId(Long userId, Long cardId);

    List<CardProgress> findByUserIdAndCardIdIn(Long userId, List<Long> cardIds);
}
