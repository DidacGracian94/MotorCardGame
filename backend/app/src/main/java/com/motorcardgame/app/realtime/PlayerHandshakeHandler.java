package com.motorcardgame.app.realtime;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Resuelve el {@link Principal} de la conexión WebSocket leyendo el query param {@code playerId}
 * de la URL de conexión — mismo nivel de confianza que el {@code playerId} de hoy en REST (no hay
 * Security de por medio en el proyecto). Sin {@code playerId}, la conexión queda como espectador
 * anónimo: sin Principal, solo llegan los mensajes de {@code /topic} público.
 */
public class PlayerHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String playerId = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("playerId");
        return (playerId == null || playerId.isBlank()) ? null : new PlayerPrincipal(playerId);
    }
}
