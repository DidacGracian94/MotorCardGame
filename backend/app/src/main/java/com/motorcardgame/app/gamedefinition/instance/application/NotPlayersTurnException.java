package com.motorcardgame.app.gamedefinition.instance.application;

import com.motorcardgame.engine.state.PlayerId;
import java.util.UUID;

public final class NotPlayersTurnException extends RuntimeException {

    public NotPlayersTurnException(UUID instanceId, PlayerId actingPlayerId, PlayerId currentPlayerId) {
        super("Player " + actingPlayerId.value() + " cannot act on instance " + instanceId
                + ": it is " + currentPlayerId.value() + "'s turn");
    }
}
