package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.dto.CreateGameInstanceRequest;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game-definitions/{gameDefinitionId}/instances")
public class GameInstanceController {

    private final GameInstanceService gameInstanceService;
    private final ObjectMapper objectMapper;

    public GameInstanceController(GameInstanceService gameInstanceService, ObjectMapper objectMapper) {
        this.gameInstanceService = gameInstanceService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<GameInstanceResponse> create(
            @PathVariable UUID gameDefinitionId, @Valid @RequestBody CreateGameInstanceRequest request)
            throws JsonProcessingException {
        GameInstance created = gameInstanceService.create(gameDefinitionId, request.versionNumber());
        GameInstanceResponse body = GameInstanceResponse.from(created, objectMapper);
        return ResponseEntity.created(URI.create("/api/instances/" + body.id())).body(body);
    }

    @GetMapping
    public List<GameInstanceResponse> listAll(@PathVariable UUID gameDefinitionId) throws JsonProcessingException {
        List<GameInstanceResponse> responses = new ArrayList<>();
        for (GameInstance instance : gameInstanceService.listByGameDefinition(gameDefinitionId)) {
            responses.add(GameInstanceResponse.from(instance, objectMapper));
        }
        return responses;
    }
}
