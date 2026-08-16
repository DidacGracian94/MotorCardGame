package com.motorcardgame.app.gamedefinition.instance.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import java.time.Instant;
import java.util.UUID;

public record GameInstanceResponse(
        UUID id,
        UUID gameDefinitionId,
        UUID gameDefinitionVersionId,
        JsonNode state,
        Instant createdAt,
        Instant endedAt) {

    public static GameInstanceResponse from(GameInstance instance, ObjectMapper objectMapper)
            throws JsonProcessingException {
        return new GameInstanceResponse(
                instance.id(),
                instance.gameDefinitionId(),
                instance.gameDefinitionVersionId(),
                objectMapper.readTree(instance.state()),
                instance.createdAt(),
                instance.endedAt());
    }
}
