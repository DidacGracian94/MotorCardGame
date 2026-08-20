package com.motorcardgame.app.gamedefinition.application;

import java.util.UUID;

public class GameDefinitionAccessDeniedException extends RuntimeException {

    public GameDefinitionAccessDeniedException(UUID id) {
        super("No tienes acceso a la GameDefinition: " + id);
    }
}
