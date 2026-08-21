package com.motorcardgame.app.aiguide.web;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
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
class AiGuideControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void get_returnsMarkdownWithConceptsAndAllCapabilities() throws Exception {
        mockMvc.perform(get("/api/ai-guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markdown", allOf(
                        containsString("GameDefinition"),
                        containsString("\"zones\""),
                        containsString("DRAW_CARDS"),
                        containsString("WINNING_CARD_OWNER"),
                        containsString("CURRENT_PLAYER"),
                        containsString("AND"))));
    }
}
