package com.motorcardgame.app.gamedefinition.instance.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record PlayerActionRequest(@NotBlank String playerId, @NotBlank String eventType, Map<String, Object> payload) {

    public PlayerActionRequest {
        payload = payload == null ? Map.of() : payload;
    }
}
