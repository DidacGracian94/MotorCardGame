package com.motorcardgame.app.gamedefinition.instance.application;

import java.util.UUID;

public final class UnknownPlayerActionException extends RuntimeException {

    public UnknownPlayerActionException(UUID instanceId, String eventType) {
        super("Action \"" + eventType + "\" is not declared as a playerAction on instance " + instanceId
                + "'s game definition");
    }
}
