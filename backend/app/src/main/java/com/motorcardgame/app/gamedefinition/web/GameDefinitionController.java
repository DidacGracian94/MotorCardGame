package com.motorcardgame.app.gamedefinition.web;

import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.web.dto.ChangeVisibilityRequest;
import com.motorcardgame.app.gamedefinition.web.dto.CreateGameDefinitionRequest;
import com.motorcardgame.app.gamedefinition.web.dto.GameDefinitionResponse;
import com.motorcardgame.app.gamedefinition.web.dto.RenameGameDefinitionRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game-definitions")
public class GameDefinitionController {

    private final GameDefinitionService gameDefinitionService;

    public GameDefinitionController(GameDefinitionService gameDefinitionService) {
        this.gameDefinitionService = gameDefinitionService;
    }

    @PostMapping
    public ResponseEntity<GameDefinitionResponse> create(@Valid @RequestBody CreateGameDefinitionRequest request) {
        UUID ownerId = currentPrincipal().userId();
        GameDefinition created = gameDefinitionService.create(ownerId, request.name(), request.slug());
        GameDefinitionResponse body = GameDefinitionResponse.from(created);
        return ResponseEntity.created(URI.create("/api/game-definitions/" + body.id())).body(body);
    }

    @GetMapping
    public List<GameDefinitionResponse> listAll() {
        TokenClaims principal = currentPrincipal();
        return gameDefinitionService.listVisibleTo(principal.userId(), principal.role()).stream()
                .map(GameDefinitionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GameDefinitionResponse getById(@PathVariable UUID id) {
        TokenClaims principal = currentPrincipal();
        return GameDefinitionResponse.from(
                gameDefinitionService.assertVisible(id, principal.userId(), principal.role()));
    }

    @PutMapping("/{id}")
    public GameDefinitionResponse rename(
            @PathVariable UUID id, @Valid @RequestBody RenameGameDefinitionRequest request) {
        TokenClaims principal = currentPrincipal();
        return GameDefinitionResponse.from(
                gameDefinitionService.rename(id, request.name(), principal.userId(), principal.role()));
    }

    @PatchMapping("/{id}/visibility")
    public GameDefinitionResponse changeVisibility(
            @PathVariable UUID id, @Valid @RequestBody ChangeVisibilityRequest request) {
        TokenClaims principal = currentPrincipal();
        return GameDefinitionResponse.from(gameDefinitionService.changeVisibility(
                id, request.visibility(), principal.userId(), principal.role()));
    }

    private static TokenClaims currentPrincipal() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
