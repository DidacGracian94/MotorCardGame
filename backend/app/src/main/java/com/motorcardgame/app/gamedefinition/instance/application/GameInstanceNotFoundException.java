package com.motorcardgame.app.gamedefinition.instance.application;

import java.util.UUID;

public final class GameInstanceNotFoundException extends RuntimeException {

    public GameInstanceNotFoundException(UUID id) {
        super("GameInstance not found: " + id);
    }
}
