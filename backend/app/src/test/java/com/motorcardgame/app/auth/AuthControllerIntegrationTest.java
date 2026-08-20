package com.motorcardgame.app.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenAccessProtectedEndpoint() throws Exception {
        String email = uniqueEmail("alice");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, "correct-horse-battery", "Alice")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode tokens = objectMapper.readTree(registerResponse);
        String accessToken = tokens.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(tokens.get("user").get("email").asText()).isEqualTo(email);

        mockMvc.perform(get("/api/game-definitions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/game-definitions").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void registeringTheSameEmailTwiceConflicts() throws Exception {
        String email = uniqueEmail("dup");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, "correct-horse-battery", "Dup")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, "correct-horse-battery", "Dup")))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        String email = uniqueEmail("bob");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, "correct-horse-battery", "Bob")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, "wrong-password")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, "correct-horse-battery")))
                .andExpect(status().isOk());
    }

    @Test
    void refreshRotatesAndRevokesThePreviousToken() throws Exception {
        String email = uniqueEmail("carol");
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, "correct-horse-battery", "Carol")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private static String uniqueEmail(String localPart) {
        return localPart + "-" + System.nanoTime() + "@example.com";
    }

    private String registerPayload(String email, String password, String displayName) throws Exception {
        return objectMapper.writeValueAsString(new RegisterPayload(email, password, displayName));
    }

    private String loginPayload(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginPayload(email, password));
    }

    private record RegisterPayload(String email, String password, String displayName) {
    }

    private record LoginPayload(String email, String password) {
    }
}
