package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import com.motorcardgame.app.gamedefinition.instance.web.dto.PlayerActionRequest;
import com.motorcardgame.engine.state.PlayerId;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instances/{instanceId}")
public class InstanceController {

    private final GameInstanceService gameInstanceService;
    private final GameInstanceResponseFactory responseFactory;

    public InstanceController(GameInstanceService gameInstanceService, GameInstanceResponseFactory responseFactory) {
        this.gameInstanceService = gameInstanceService;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public GameInstanceResponse getById(@PathVariable UUID instanceId, @RequestParam(required = false) String asPlayer)
            throws JsonProcessingException {
        return responseFactory.forViewer(gameInstanceService.getById(instanceId), asPlayer);
    }

    @PostMapping("/actions")
    public GameInstanceResponse applyAction(
            @PathVariable UUID instanceId,
            @Valid @RequestBody PlayerActionRequest request,
            @RequestParam(required = false) String asPlayer)
            throws JsonProcessingException {
        GameInstance updated = gameInstanceService.applyAction(
                instanceId, new PlayerId(request.playerId()), request.eventType(), request.payload());
        String viewerPlayerId = asPlayer != null ? asPlayer : request.playerId();
        return responseFactory.forViewer(updated, viewerPlayerId);
    }
}
