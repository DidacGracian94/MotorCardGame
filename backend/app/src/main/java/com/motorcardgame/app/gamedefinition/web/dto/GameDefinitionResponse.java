package com.motorcardgame.app.gamedefinition.web.dto;

import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
import java.time.Instant;
import java.util.UUID;

public record GameDefinitionResponse(
        UUID id,
        UUID ownerId,
        String name,
        String slug,
        GameDefinitionVisibility visibility,
        Instant createdAt,
        Instant updatedAt) {

    public static GameDefinitionResponse from(GameDefinition gameDefinition) {
        return new GameDefinitionResponse(
                gameDefinition.id(),
                gameDefinition.ownerId(),
                gameDefinition.name(),
                gameDefinition.slug(),
                gameDefinition.visibility(),
                gameDefinition.createdAt(),
                gameDefinition.updatedAt());
    }
}
