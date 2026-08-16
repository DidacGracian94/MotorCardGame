package com.motorcardgame.app.gamedefinition.version.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record PublishGameDefinitionVersionRequest(@NotNull JsonNode config) {
}
