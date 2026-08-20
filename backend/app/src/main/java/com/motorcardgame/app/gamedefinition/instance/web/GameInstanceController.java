package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.dto.CreateGameInstanceRequest;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game-definitions/{gameDefinitionId}/instances")
public class GameInstanceController {

    private final GameInstanceService gameInstanceService;
    private final GameInstanceResponseFactory responseFactory;

    public GameInstanceController(GameInstanceService gameInstanceService, GameInstanceResponseFactory responseFactory) {
        this.gameInstanceService = gameInstanceService;
        this.responseFactory = responseFactory;
    }

    @PostMapping
    public ResponseEntity<GameInstanceResponse> create(
            @PathVariable UUID gameDefinitionId,
            @Valid @RequestBody CreateGameInstanceRequest request,
            @RequestParam(required = false) String asPlayer)
            throws JsonProcessingException {
        List<Player> players = request.players().stream()
                .map(p -> new Player(new PlayerId(p.id()), p.displayName()))
                .toList();
        TokenClaims principal = currentPrincipal();
        GameInstance created = gameInstanceService.create(
                gameDefinitionId, request.versionNumber(), players, principal.userId(), principal.role());
        GameInstanceResponse body = responseFactory.forViewer(created, asPlayer);
        return ResponseEntity.created(URI.create("/api/instances/" + body.id())).body(body);
    }

    @GetMapping
    public List<GameInstanceResponse> listAll(@PathVariable UUID gameDefinitionId) throws JsonProcessingException {
        TokenClaims principal = currentPrincipal();
        List<GameInstanceResponse> responses = new ArrayList<>();
        for (GameInstance instance : gameInstanceService.listByGameDefinition(
                gameDefinitionId, principal.userId(), principal.role())) {
            responses.add(responseFactory.forViewer(instance, null));
        }
        return responses;
    }

    private static TokenClaims currentPrincipal() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
