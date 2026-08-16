package com.motorcardgame.app.gamedefinition.application;

import java.util.UUID;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(UUID ownerId, String slug) {
        super("El owner " + ownerId + " ya tiene una GameDefinition con slug: " + slug);
    }
}
