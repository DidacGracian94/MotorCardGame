package com.motorcardgame.app.gamedefinition.instance.application;

import com.motorcardgame.engine.state.PlayerId;
import java.util.UUID;

public final class PlayerActionRejectedException extends RuntimeException {

    public PlayerActionRejectedException(UUID instanceId, PlayerId actingPlayerId, String eventType) {
        super("Player " + actingPlayerId.value() + " cannot perform action \"" + eventType
                + "\" on instance " + instanceId + ": no rule allowed it");
    }
}
