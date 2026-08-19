package com.motorcardgame.app.gamedefinition.instance.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.GameStateVisibility;
import com.motorcardgame.engine.state.ZoneVisibility;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameInstanceResponse(
        UUID id,
        UUID gameDefinitionId,
        UUID gameDefinitionVersionId,
        JsonNode state,
        Instant createdAt,
        Instant endedAt) {

    /**
     * @param viewerPlayerId jugador para el que se filtra el estado ({@code null} = vista de
     *     espectador)
     * @param zoneVisibilityByName visibilidad declarada de cada zona en la config de la versión
     *     con la que se creó la instancia (ver {@code GameInstanceResponseFactory}, que resuelve
     *     este mapa para no repetirlo en cada sitio que construye una respuesta)
     */
    public static GameInstanceResponse from(
            GameInstance instance,
            ObjectMapper objectMapper,
            String viewerPlayerId,
            Map<String, ZoneVisibility> zoneVisibilityByName)
            throws JsonProcessingException {
        JsonNode fullState = objectMapper.readTree(instance.state());
        return new GameInstanceResponse(
                instance.id(),
                instance.gameDefinitionId(),
                instance.gameDefinitionVersionId(),
                GameStateVisibility.forViewer(fullState, viewerPlayerId, zoneVisibilityByName),
                instance.createdAt(),
                instance.endedAt());
    }
}
