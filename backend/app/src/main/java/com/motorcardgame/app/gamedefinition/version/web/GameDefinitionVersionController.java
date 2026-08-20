package com.motorcardgame.app.gamedefinition.version.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.gamedefinition.version.web.dto.GameDefinitionVersionResponse;
import com.motorcardgame.app.gamedefinition.version.web.dto.PublishGameDefinitionVersionRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game-definitions/{gameDefinitionId}/versions")
public class GameDefinitionVersionController {

    private final GameDefinitionVersionService gameDefinitionVersionService;
    private final ObjectMapper objectMapper;

    public GameDefinitionVersionController(
            GameDefinitionVersionService gameDefinitionVersionService, ObjectMapper objectMapper) {
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<GameDefinitionVersionResponse> publish(
            @PathVariable UUID gameDefinitionId, @Valid @RequestBody PublishGameDefinitionVersionRequest request)
            throws JsonProcessingException {
        String configJson = objectMapper.writeValueAsString(request.config());
        TokenClaims principal = currentPrincipal();
        GameDefinitionVersion published = gameDefinitionVersionService.publish(
                gameDefinitionId, configJson, principal.userId(), principal.role());
        GameDefinitionVersionResponse body = toResponse(published);
        return ResponseEntity.created(URI.create(
                        "/api/game-definitions/" + gameDefinitionId + "/versions/" + body.versionNumber()))
                .body(body);
    }

    @GetMapping
    public List<GameDefinitionVersionResponse> listAll(@PathVariable UUID gameDefinitionId)
            throws JsonProcessingException {
        TokenClaims principal = currentPrincipal();
        List<GameDefinitionVersionResponse> responses = new ArrayList<>();
        for (GameDefinitionVersion version : gameDefinitionVersionService.listByGameDefinition(
                gameDefinitionId, principal.userId(), principal.role())) {
            responses.add(toResponse(version));
        }
        return responses;
    }

    @GetMapping("/{versionNumber}")
    public GameDefinitionVersionResponse getByVersionNumber(
            @PathVariable UUID gameDefinitionId, @PathVariable int versionNumber) throws JsonProcessingException {
        TokenClaims principal = currentPrincipal();
        return toResponse(gameDefinitionVersionService.getByVersionNumber(
                gameDefinitionId, versionNumber, principal.userId(), principal.role()));
    }

    private GameDefinitionVersionResponse toResponse(GameDefinitionVersion version) throws JsonProcessingException {
        return new GameDefinitionVersionResponse(
                version.id(),
                version.gameDefinitionId(),
                version.versionNumber(),
                objectMapper.readTree(version.config()),
                version.createdAt(),
                version.publishedAt());
    }

    private static TokenClaims currentPrincipal() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
