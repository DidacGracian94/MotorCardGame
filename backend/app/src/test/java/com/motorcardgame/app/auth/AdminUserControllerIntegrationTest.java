package com.motorcardgame.app.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.auth.web.dto.ChangeUserRoleRequest;
import com.motorcardgame.app.testsupport.TestAuth;
import com.motorcardgame.app.testsupport.WithMockUserId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUserId
class AdminUserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerUser(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterPayload(
                                email, "correct-horse-battery-staple", "Test User"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("user").get("id").asText();
    }

    @Test
    void listAll_returnsForbidden_forNormalUser() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(TestAuth.as(UUID.randomUUID(), Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAll_returnsUnauthorized_forAnonymousRequest() throws Exception {
        // Spring Security trata el acceso denegado de un anónimo como "no autenticado" (401), no
        // como "autenticado pero sin permiso" (403) — eso último es lo que prueba
        // listAll_returnsForbidden_forNormalUser, con un usuario real sin rol admin.
        mockMvc.perform(get("/api/admin/users").with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_returnsUsers_forAdmin() throws Exception {
        registerUser("listado-" + System.nanoTime() + "@example.com");

        mockMvc.perform(get("/api/admin/users").with(TestAuth.as(UUID.randomUUID(), Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void changeRole_promotesUserToAdmin() throws Exception {
        String userId = registerUser("promocionar-" + System.nanoTime() + "@example.com");
        UUID adminId = UUID.randomUUID();

        mockMvc.perform(put("/api/admin/users/{id}/role", userId)
                        .with(TestAuth.as(adminId, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeUserRoleRequest(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changeRole_returnsBadRequest_whenChangingOwnRole() throws Exception {
        UUID adminId = UUID.randomUUID();

        mockMvc.perform(put("/api/admin/users/{id}/role", adminId)
                        .with(TestAuth.as(adminId, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeUserRoleRequest(Role.USER))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeRole_returnsForbidden_forNormalUser() throws Exception {
        String userId = registerUser("no-autorizado-" + System.nanoTime() + "@example.com");

        mockMvc.perform(put("/api/admin/users/{id}/role", userId)
                        .with(TestAuth.as(UUID.randomUUID(), Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeUserRoleRequest(Role.ADMIN))))
                .andExpect(status().isForbidden());
    }

    private record RegisterPayload(String email, String password, String displayName) {
    }
}
