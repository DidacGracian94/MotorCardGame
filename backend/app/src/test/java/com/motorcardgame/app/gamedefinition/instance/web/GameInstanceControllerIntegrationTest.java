package com.motorcardgame.app.gamedefinition.instance.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.web.dto.CreateGameDefinitionRequest;
import com.motorcardgame.app.gamedefinition.web.dto.GameDefinitionResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class GameInstanceControllerIntegrationTest {

    private static final List<Map<String, String>> ONE_PLAYER = List.of(Map.of("id", "alice", "displayName", "Alice"));

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createGameDefinition(String slug) throws Exception {
        CreateGameDefinitionRequest request = new CreateGameDefinitionRequest(UUID.randomUUID(), "Mi Juego", slug);
        String body = mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, GameDefinitionResponse.class).id();
    }

    private void publishVersion(UUID gameDefinitionId, Object config) throws Exception {
        mockMvc.perform(post("/api/game-definitions/{id}/versions", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("config", config))))
                .andExpect(status().isCreated());
    }

    private String createInstance(UUID gameDefinitionId, int versionNumber, Object players) throws Exception {
        return mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", versionNumber, "players", players))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void create_returnsCreatedInstance_withRealInitialState() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-1");
        publishVersion(gameDefinitionId, Map.of("rules", List.of(), "zones", List.of()));

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1, "players", ONE_PLAYER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameDefinitionId").value(gameDefinitionId.toString()))
                .andExpect(jsonPath("$.state.players[0].id").value("alice"))
                .andExpect(jsonPath("$.state.currentPlayerIndex").value(0));
    }

    @Test
    void create_returnsNotFound_whenGameDefinitionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/game-definitions/{id}/instances", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1, "players", ONE_PLAYER))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsNotFound_whenVersionDoesNotExist() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-2");

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1, "players", ONE_PLAYER))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsBadRequest_whenNoPlayersProvided() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-6");
        publishVersion(gameDefinitionId, Map.of("rules", List.of(), "zones", List.of()));

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1, "players", List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_dealsInitialHands_whenConfigDeclaresZonesCardsAndGameStartedRule() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-7");
        Map<String, Object> config = Map.of(
                "zones", List.of(
                        Map.of("name", "deck", "ownership", "SHARED"),
                        Map.of("name", "hand", "ownership", "PER_PLAYER")),
                "cards", List.of(Map.of("id", "red", "zone", "deck", "count", 10, "attributes", Map.of("color", "RED"))),
                "rules", List.of(Map.of(
                        "event", "GAME_STARTED",
                        "condition", Map.of("type", "AND", "conditions", List.of()),
                        "target", Map.of("type", "ALL_PLAYERS"),
                        "action", Map.of(
                                "type", "REPEAT", "times", 3,
                                "action", Map.of(
                                        "type", "DRAW_CARDS", "count", 1,
                                        "from", Map.of("name", "deck", "ownership", "SHARED"),
                                        "to", Map.of("name", "hand", "ownership", "PER_PLAYER"))))));
        publishVersion(gameDefinitionId, config);
        List<Map<String, String>> players = List.of(
                Map.of("id", "alice", "displayName", "Alice"), Map.of("id", "bob", "displayName", "Bob"));

        String body = createInstance(gameDefinitionId, 1, players);
        JsonNode state = objectMapper.readTree(body).get("state");

        assertHandSize(state, "alice", 3);
        assertHandSize(state, "bob", 3);
        assertEquals(4, state.get("sharedZones").get("deck").size());
    }

    private static void assertHandSize(JsonNode state, String playerId, int expectedSize) {
        JsonNode hand = state.get("perPlayerZones").get("hand").get(playerId);
        assertEquals(expectedSize, hand.size());
    }

    @Test
    void listAll_returnsInstancesForDefinition() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-4");
        publishVersion(gameDefinitionId, Map.of("rules", List.of(), "zones", List.of()));
        createInstance(gameDefinitionId, 1, ONE_PLAYER);

        mockMvc.perform(get("/api/game-definitions/{id}/instances", gameDefinitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listAll_returnsNotFound_whenGameDefinitionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/game-definitions/{id}/instances", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returnsInstance() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-5");
        publishVersion(gameDefinitionId, Map.of("rules", List.of(), "zones", List.of()));
        String body = createInstance(gameDefinitionId, 1, ONE_PLAYER);
        String instanceId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(get("/api/instances/{id}", instanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(instanceId));
    }

    @Test
    void getById_returnsNotFound_whenInstanceDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/instances/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private static Map<String, Object> drawCardsOnRequestConfig() {
        return Map.of(
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

    @Test
    void applyAction_drawsCardForCurrentPlayer_whenEventMatchesConfiguredRule() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-8");
        publishVersion(gameDefinitionId, drawCardsOnRequestConfig());
        List<Map<String, String>> players = List.of(
                Map.of("id", "alice", "displayName", "Alice"), Map.of("id", "bob", "displayName", "Bob"));
        String body = createInstance(gameDefinitionId, 1, players);
        String instanceId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(post("/api/instances/{id}/actions", instanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("playerId", "alice", "eventType", "PLAYER_REQUESTED_DRAW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.perPlayerZones.hand.alice.length()").value(1))
                .andExpect(jsonPath("$.state.sharedZones.deck.length()").value(9));
    }

    @Test
    void applyAction_returnsConflict_whenActingPlayerIsNotCurrentPlayer() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-9");
        publishVersion(gameDefinitionId, drawCardsOnRequestConfig());
        List<Map<String, String>> players = List.of(
                Map.of("id", "alice", "displayName", "Alice"), Map.of("id", "bob", "displayName", "Bob"));
        String body = createInstance(gameDefinitionId, 1, players);
        String instanceId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(post("/api/instances/{id}/actions", instanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("playerId", "bob", "eventType", "PLAYER_REQUESTED_DRAW"))))
                .andExpect(status().isConflict());
    }

    @Test
    void applyAction_returnsNotFound_whenInstanceDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/instances/{id}/actions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("playerId", "alice", "eventType", "PLAYER_REQUESTED_DRAW"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void applyAction_returnsBadRequest_whenPlayerIdIsBlank() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-10");
        publishVersion(gameDefinitionId, drawCardsOnRequestConfig());
        String body = createInstance(gameDefinitionId, 1, ONE_PLAYER);
        String instanceId = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(post("/api/instances/{id}/actions", instanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("playerId", "", "eventType", "PLAYER_REQUESTED_DRAW"))))
                .andExpect(status().isBadRequest());
    }
}
