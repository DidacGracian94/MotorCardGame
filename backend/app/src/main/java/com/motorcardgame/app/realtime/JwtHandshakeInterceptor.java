package com.motorcardgame.app.realtime;

import com.motorcardgame.app.auth.application.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Exige un {@code token} (JWT de acceso) válido como query param para aceptar el handshake — un
 * WebSocket nativo del navegador no puede mandar la cabecera {@code Authorization}, así que el
 * token viaja en la URL de conexión, igual que ya viaja {@code playerId}. Sin esto, conectar
 * directo al WS se saltaría por completo la autenticación que exige el resto de {@code /api/**}.
 */
class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (token == null || jwtService.validateAccessToken(token).isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
