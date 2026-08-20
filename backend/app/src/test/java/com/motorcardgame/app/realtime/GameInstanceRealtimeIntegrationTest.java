package com.motorcardgame.app.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica que crear una instancia y aplicar una acción empujan de verdad por WebSocket: la vista
 * de espectador a {@code /topic/games/{id}/public} y la vista propia de cada jugador a
 * {@code /user/queue/games/{id}/private} — con el mismo filtrado de mano que ya cubre
 * {@code GameInstanceControllerIntegrationTest} para REST. Todo el flujo pasa por HTTP/WS reales
 * (puerto aleatorio), no por MockMvc, porque STOMP necesita una conexión de socket de verdad.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameInstanceRealtimeIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;
    private String accessToken;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        accessToken = registerAndGetAccessToken();
    }

    private String httpUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private String registerAndGetAccessToken() {
        Map<String, Object> request = Map.of(
                "email", "realtime-" + UUID.randomUUID() + "@example.com",
                "password", "correct-horse-battery-staple",
                "displayName", "Realtime Test User");
        ResponseEntity<Map> response = restTemplate.postForEntity(httpUrl("/api/auth/register"), request, Map.class);
        return (String) response.getBody().get("accessToken");
    }

    private HttpEntity<Map<String, Object>> authenticated(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private UUID createGameDefinition(String slug) {
        Map<String, Object> request = Map.of("name", "Mi Juego", "slug", slug);
        ResponseEntity<Map> response =
                restTemplate.postForEntity(httpUrl("/api/game-definitions"), authenticated(request), Map.class);
        return UUID.fromString((String) response.getBody().get("id"));
    }

    private void publishVersion(UUID gameDefinitionId, Object config) {
        restTemplate.postForEntity(
                httpUrl("/api/game-definitions/" + gameDefinitionId + "/versions"),
                authenticated(Map.of("config", config)),
                Map.class);
    }

    private UUID createInstance(UUID gameDefinitionId, Object players) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                httpUrl("/api/game-definitions/" + gameDefinitionId + "/instances"),
                authenticated(Map.of("versionNumber", 1, "players", players)),
                Map.class);
        return UUID.fromString((String) response.getBody().get("id"));
    }

    private static Map<String, Object> drawCardsOnRequestConfig() {
        return Map.of(
                "playerActions", List.of("PLAYER_REQUESTED_DRAW"),
                "zones", List.of(
                        Map.of("name", "deck", "ownership", "SHARED"),
                        Map.of("name", "hand", "ownership", "PER_PLAYER")),
                "cards", List.of(Map.of("id", "red", "zone", "deck", "count", 10, "attributes", Map.of("color", "RED"))),
                "rules", List.of(Map.of(
                        "event", "PLAYER_REQUESTED_DRAW",
                        "condition", Map.of("type", "AND", "conditions", List.of()),
                        "target", Map.of("type", "CURRENT_PLAYER"),
                        "action", Map.of(
                                "type", "DRAW_CARDS", "count", 1,
                                "from", Map.of("name", "deck", "ownership", "SHARED"),
                                "to", Map.of("name", "hand", "ownership", "PER_PLAYER")))));
    }

    private StompSession connect(String playerId) throws Exception {
        String query = "?token=" + accessToken + (playerId == null ? "" : "&playerId=" + playerId);
        String url = "ws://localhost:" + port + "/ws" + query;
        return stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
    }

    private <T> CompletableFuture<T> subscribe(StompSession session, String destination, Class<T> payloadType) {
        CompletableFuture<T> future = new CompletableFuture<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return payloadType;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete(payloadType.cast(payload));
            }
        });
        return future;
    }

    @Test
    void applyAction_pushesPrivateStateToActingPlayer_andFilteredPublicStateToSpectators() throws Exception {
        UUID gameDefinitionId = createGameDefinition("realtime-1");
        publishVersion(gameDefinitionId, drawCardsOnRequestConfig());
        List<Map<String, String>> players = List.of(
                Map.of("id", "alice", "displayName", "Alice"), Map.of("id", "bob", "displayName", "Bob"));
        UUID instanceId = createInstance(gameDefinitionId, players);

        StompSession aliceSession = connect("alice");
        StompSession spectatorSession = connect(null);
        CompletableFuture<Map> alicePrivate =
                subscribe(aliceSession, "/user/queue/games/" + instanceId + "/private", Map.class);
        CompletableFuture<Map> spectatorPublic =
                subscribe(spectatorSession, "/topic/games/" + instanceId + "/public", Map.class);
        // da tiempo a que las suscripciones lleguen al broker antes de disparar la acción
        Thread.sleep(300);

        HttpEntity<Map<String, Object>> actionRequest =
                authenticated(Map.of("playerId", "alice", "eventType", "PLAYER_REQUESTED_DRAW"));
        restTemplate.postForEntity(httpUrl("/api/instances/" + instanceId + "/actions"), actionRequest, Map.class);

        Map alicePayload = alicePrivate.get(5, TimeUnit.SECONDS);
        Map spectatorPayload = spectatorPublic.get(5, TimeUnit.SECONDS);

        JsonNode aliceState = objectMapper.valueToTree(alicePayload.get("state"));
        assertThat(aliceState.get("perPlayerZones").get("hand").get("alice").size()).isEqualTo(1);

        JsonNode spectatorState = objectMapper.valueToTree(spectatorPayload.get("state"));
        assertThat(spectatorState.get("perPlayerZones").get("hand").get("alice").has("hiddenCount")).isTrue();

        aliceSession.disconnect();
        spectatorSession.disconnect();
    }
}
