package com.motorcardgame.app.gamedefinition.version.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record GameDefinitionVersionResponse(
        UUID id,
        UUID gameDefinitionId,
        int versionNumber,
        JsonNode config,
        Instant createdAt,
        Instant publishedAt) {
}
