package com.motorcardgame.app.gamedefinition.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.web.dto.CreateGameDefinitionRequest;
import com.motorcardgame.app.gamedefinition.web.dto.RenameGameDefinitionRequest;
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
class GameDefinitionControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createThenGetById_returnsCreatedGameDefinition() throws Exception {
        CreateGameDefinitionRequest request =
                new CreateGameDefinitionRequest(UUID.randomUUID(), "Mi Juego", "mi-juego-integration");

        String createdBody = mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mi Juego"))
                .andExpect(jsonPath("$.slug").value("mi-juego-integration"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createdBody).get("id").asText();

        mockMvc.perform(get("/api/game-definitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("mi-juego-integration"));
    }

    @Test
    void create_returnsConflict_whenSlugAlreadyExists() throws Exception {
        CreateGameDefinitionRequest request =
                new CreateGameDefinitionRequest(UUID.randomUUID(), "Duplicado", "slug-duplicado");

        mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_returnsBadRequest_whenSlugHasInvalidFormat() throws Exception {
        CreateGameDefinitionRequest request =
                new CreateGameDefinitionRequest(UUID.randomUUID(), "Mi Juego", "Slug Invalido!");

        mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returnsNotFound_whenGameDefinitionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/game-definitions/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rename_updatesNameAndListIncludesIt() throws Exception {
        CreateGameDefinitionRequest createRequest =
                new CreateGameDefinitionRequest(UUID.randomUUID(), "Nombre Viejo", "mi-juego-rename");

        String createdBody = mockMvc.perform(post("/api/game-definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = objectMapper.readTree(createdBody).get("id").asText();

        mockMvc.perform(put("/api/game-definitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RenameGameDefinitionRequest("Nombre Nuevo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre Nuevo"));

        mockMvc.perform(get("/api/game-definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + id + "')].name").value("Nombre Nuevo"));
    }
}
