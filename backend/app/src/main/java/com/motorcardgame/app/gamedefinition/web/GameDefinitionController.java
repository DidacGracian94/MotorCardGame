package com.motorcardgame.app.gamedefinition.web;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.web.dto.CreateGameDefinitionRequest;
import com.motorcardgame.app.gamedefinition.web.dto.GameDefinitionResponse;
import com.motorcardgame.app.gamedefinition.web.dto.RenameGameDefinitionRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
        GameDefinition created = gameDefinitionService.create(request.ownerId(), request.name(), request.slug());
        GameDefinitionResponse body = GameDefinitionResponse.from(created);
        return ResponseEntity.created(URI.create("/api/game-definitions/" + body.id())).body(body);
    }

    @GetMapping
    public List<GameDefinitionResponse> listAll() {
        return gameDefinitionService.listAll().stream().map(GameDefinitionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GameDefinitionResponse getById(@PathVariable UUID id) {
        return GameDefinitionResponse.from(gameDefinitionService.getById(id));
    }

    @PutMapping("/{id}")
    public GameDefinitionResponse rename(
            @PathVariable UUID id, @Valid @RequestBody RenameGameDefinitionRequest request) {
        return GameDefinitionResponse.from(gameDefinitionService.rename(id, request.name()));
    }
}
