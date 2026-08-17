package com.motorcardgame.app.gamedefinition.version.application;

import java.util.UUID;

public class GameDefinitionVersionNotFoundException extends RuntimeException {

    public GameDefinitionVersionNotFoundException(UUID gameDefinitionId, int versionNumber) {
        super("GameDefinitionVersion no encontrada: gameDefinitionId=" + gameDefinitionId
                + ", versionNumber=" + versionNumber);
    }

    public GameDefinitionVersionNotFoundException(UUID versionId) {
        super("GameDefinitionVersion no encontrada: id=" + versionId);
    }
}
