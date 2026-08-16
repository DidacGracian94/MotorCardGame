package com.motorcardgame.app.gamedefinition.application;

import java.util.UUID;

public class GameDefinitionNotFoundException extends RuntimeException {

    public GameDefinitionNotFoundException(UUID id) {
        super("GameDefinition no encontrada: " + id);
    }
}
