package com.motorcardgame.app.gamedefinition.version.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.web.dto.CreateGameDefinitionRequest;
import com.motorcardgame.app.gamedefinition.web.dto.GameDefinitionResponse;
import com.motorcardgame.app.testsupport.WithMockUserId;
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
@WithMockUserId
class GameDefinitionVersionControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createGameDefinition(String slug) throws Exception {
        CreateGameDefinitionRequest request = new CreateGameDefinitionRequest("Mi Juego", slug);
        String body = mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, GameDefinitionResponse.class).id();
    }

    @Test
    void publish_returnsVersionOne_forFirstVersion() throws Exception {
        UUID gameDefinitionId = createGameDefinition("juego-version-1");

        mockMvc.perform(post("/api/game-definitions/{id}/versions", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("config", Map.of("rules", List.of(), "zones", List.of())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.gameDefinitionId").value(gameDefinitionId.toString()));
    }

    @Test
    void publish_incrementsVersionNumber_onSecondPublish() throws Exception {
        UUID gameDefinitionId = createGameDefinition("juego-version-2");
        String requestBody = objectMapper.writeValueAsString(
                Map.of("config", Map.of("rules", List.of(), "zones", List.of())));

        mockMvc.perform(post("/api/game-definitions/{id}/versions", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1));

        mockMvc.perform(post("/api/game-definitions/{id}/versions", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(2));

        mockMvc.perform(get("/api/game-definitions/{id}/versions", gameDefinitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void publish_returnsBadRequest_whenConfigReferencesUnknownCapability() throws Exception {
        UUID gameDefinitionId = createGameDefinition("juego-version-invalida");
        Map<String, Object> config = Map.of(
                "zones", List.of(),
                "rules", List.of(Map.of(
                        "event", "TURN_STARTED",
                        "condition", Map.of("type", "UNKNOWN_CONDITION"),
                        "target", Map.of("type", "CURRENT_PLAYER"),
                        "action", Map.of("type", "NEXT_PLAYER"))));

        mockMvc.perform(post("/api/game-definitions/{id}/versions", gameDefinitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("config", config))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publish_returnsNotFound_whenGameDefinitionDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/game-definitions/{id}/versions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("config", Map.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByVersionNumber_returnsNotFound_whenVersionDoesNotExist() throws Exception {
        UUID gameDefinitionId = createGameDefinition("juego-sin-versiones");

        mockMvc.perform(get("/api/game-definitions/{id}/versions/{versionNumber}", gameDefinitionId, 1))
                .andExpect(status().isNotFound());
    }
}
