package com.motorcardgame.app.gamedefinition.version.application;

import java.util.UUID;

public class VersionPublishConflictException extends RuntimeException {

    public VersionPublishConflictException(UUID gameDefinitionId, int attempts) {
        super("No se pudo publicar una versión nueva para GameDefinition " + gameDefinitionId
                + " tras " + attempts + " intentos (conflicto de version_number concurrente)");
    }
}
