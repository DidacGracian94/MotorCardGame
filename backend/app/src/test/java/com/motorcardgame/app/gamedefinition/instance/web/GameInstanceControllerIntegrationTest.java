package com.motorcardgame.app.gamedefinition.instance.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private String createInstance(UUID gameDefinitionId, int versionNumber) throws Exception {
        return mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", versionNumber))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void create_returnsCreatedInstance_withPlaceholderState() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-1");
        publishVersion(gameDefinitionId, Map.of("rules", List.of()));

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameDefinitionId").value(gameDefinitionId.toString()))
                .andExpect(jsonPath("$.state.status").value("NOT_STARTED"));
    }

    @Test
    void create_returnsNotFound_whenGameDefinitionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/game-definitions/{id}/instances", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsNotFound_whenVersionDoesNotExist() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-2");

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsBadRequest_whenConfigReferencesUnknownCapability() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-3");
        publishVersion(gameDefinitionId, Map.of("rules", List.of(Map.of(
                "event", "TURN_STARTED",
                "condition", Map.of("type", "UNKNOWN_CONDITION"),
                "target", Map.of("type", "CURRENT_PLAYER"),
                "action", Map.of("type", "NEXT_PLAYER")))));

        mockMvc.perform(post("/api/game-definitions/{id}/instances", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("versionNumber", 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAll_returnsInstancesForDefinition() throws Exception {
        UUID gameDefinitionId = createGameDefinition("uno-instancias-4");
        publishVersion(gameDefinitionId, Map.of("rules", List.of()));
        createInstance(gameDefinitionId, 1);

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
        publishVersion(gameDefinitionId, Map.of("rules", List.of()));
        String body = createInstance(gameDefinitionId, 1);
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
}
