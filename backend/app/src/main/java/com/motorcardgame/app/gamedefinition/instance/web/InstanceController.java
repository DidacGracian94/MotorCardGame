package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances/{instanceId}")
public class InstanceController {

    private final GameInstanceService gameInstanceService;
    private final ObjectMapper objectMapper;

    public InstanceController(GameInstanceService gameInstanceService, ObjectMapper objectMapper) {
        this.gameInstanceService = gameInstanceService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public GameInstanceResponse getById(@PathVariable UUID instanceId) throws JsonProcessingException {
        return GameInstanceResponse.from(gameInstanceService.getById(instanceId), objectMapper);
    }
}
