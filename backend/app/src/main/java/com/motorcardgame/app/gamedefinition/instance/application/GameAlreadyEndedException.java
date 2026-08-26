package com.motorcardgame.app.gamedefinition.instance.application;

import java.util.UUID;

public final class GameAlreadyEndedException extends RuntimeException {

    public GameAlreadyEndedException(UUID instanceId) {
        super("Game instance " + instanceId + " has already ended");
    }
}
