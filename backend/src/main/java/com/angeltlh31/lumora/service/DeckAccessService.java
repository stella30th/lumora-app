package com.angeltlh31.lumora.service;

import com.angeltlh31.lumora.entity.Deck;
import com.angeltlh31.lumora.exception.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class DeckAccessService {

    public void verifyOwnership(Deck deck, Long userId) {
        if (!deck.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify deck id=" + deck.getId());
        }
    }

    public void verifyReadAccess(Deck deck, Long requesterId) {
        boolean isOwner = deck.getOwner().getId().equals(requesterId);
        if (!deck.isPublic() && !isOwner) {
            throw new ForbiddenException(
                    "You do not have permission to view deck id=" + deck.getId());
        }
    }
}
