package com.motorcardgame.app.gamedefinition.instance.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.GameInstanceResponseFactory;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Tras el commit real de create/applyAction (no antes, de ahí {@code AFTER_COMMIT}: un jugador no
 * debe ver un estado que luego se revierte), empuja el {@code GameInstance} actualizado por
 * WebSocket: la vista de espectador a {@code /topic/games/{id}/public} y, además, la vista propia
 * de cada jugador declarado en {@code state.players} a {@code /queue/games/{id}/private} (no-op
 * silencioso si ese jugador no está conectado). Reutiliza {@link GameInstanceResponseFactory}, el
 * mismo camino — con el mismo filtrado de {@code GameStateVisibility} — que ya usa la API REST.
 */
@Component
public class GameInstanceRealtimePublisher {

    private final GameInstanceService gameInstanceService;
    private final GameInstanceResponseFactory responseFactory;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public GameInstanceRealtimePublisher(
            GameInstanceService gameInstanceService,
            GameInstanceResponseFactory responseFactory,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper) {
        this.gameInstanceService = gameInstanceService;
        this.responseFactory = responseFactory;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameInstanceUpdated(GameInstanceUpdatedEvent event) {
        try {
            GameInstance instance = gameInstanceService.getById(event.instanceId());
            JsonNode fullState = objectMapper.readTree(instance.state());

            GameInstanceResponse publicResponse = responseFactory.forViewer(instance, null);
            messagingTemplate.convertAndSend("/topic/games/" + event.instanceId() + "/public", publicResponse);

            for (JsonNode playerNode : fullState.path("players")) {
                String playerId = playerNode.get("id").asText();
                GameInstanceResponse privateResponse = responseFactory.forViewer(instance, playerId);
                messagingTemplate.convertAndSendToUser(
                        playerId, "/queue/games/" + event.instanceId() + "/private", privateResponse);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to build realtime game instance payload", e);
        }
    }
}
