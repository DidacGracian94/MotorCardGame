package com.motorcardgame.app.gamedefinition.instance.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.GameStateVisibility;
import java.time.Instant;
import java.util.UUID;

public record GameInstanceResponse(
        UUID id,
        UUID gameDefinitionId,
        UUID gameDefinitionVersionId,
        JsonNode state,
        Instant createdAt,
        Instant endedAt) {

    /**
     * @param viewerPlayerId jugador para el que se filtra el estado (su mano se ve completa, las
     *     demás quedan como {@code hiddenCount}); {@code null} = vista de espectador (todas
     *     ocultas).
     */
    public static GameInstanceResponse from(GameInstance instance, ObjectMapper objectMapper, String viewerPlayerId)
            throws JsonProcessingException {
        JsonNode fullState = objectMapper.readTree(instance.state());
        return new GameInstanceResponse(
                instance.id(),
                instance.gameDefinitionId(),
                instance.gameDefinitionVersionId(),
                GameStateVisibility.forViewer(fullState, viewerPlayerId),
                instance.createdAt(),
                instance.endedAt());
    }
}
