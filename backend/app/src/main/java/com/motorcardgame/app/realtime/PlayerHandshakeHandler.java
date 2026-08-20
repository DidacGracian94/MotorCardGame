package com.motorcardgame.app.realtime;

import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Resuelve el {@link Principal} de la conexión WebSocket leyendo el query param {@code playerId}
 * de la URL de conexión. {@code playerId} identifica el asiento de la partida que se quiere
 * escuchar (una cuenta puede controlar varios asientos, o ninguno) — es un concepto distinto del
 * usuario autenticado, que se exige aparte en {@link JwtHandshakeInterceptor} antes de llegar
 * aquí. Sin {@code playerId}, la conexión queda como espectador: sin Principal, solo llegan los
 * mensajes de {@code /topic} público.
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
