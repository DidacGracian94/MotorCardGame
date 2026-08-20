package com.motorcardgame.app.capability.web;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.motorcardgame.app.testsupport.WithMockUserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUserId
class CapabilitiesControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void list_returnsStandardCapabilities() throws Exception {
        mockMvc.perform(get("/api/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actions[*].name", hasItems(
                        "DRAW_CARDS", "NEXT_PLAYER", "MOVE_CARD", "SEQUENCE", "REPEAT")))
                .andExpect(jsonPath("$.conditions[*].name", hasItems(
                        "ZONE_IS_EMPTY", "CARD_ATTRIBUTE_EQUALS", "CARD_ATTRIBUTE_MATCHES_ZONE",
                        "AND", "OR", "NOT")))
                .andExpect(jsonPath("$.targets[*].name", hasItems("CURRENT_PLAYER", "ALL_PLAYERS")));
    }
}
