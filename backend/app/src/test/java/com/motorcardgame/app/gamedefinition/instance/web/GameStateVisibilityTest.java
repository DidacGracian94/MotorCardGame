package com.motorcardgame.app.gamedefinition.instance.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.engine.state.ZoneVisibility;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameStateVisibilityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode fixture() throws Exception {
        return objectMapper.readTree("""
                {
                  "players": [{"id": "alice", "displayName": "Alice"}, {"id": "bob", "displayName": "Bob"}],
                  "currentPlayerIndex": 0,
                  "direction": 1,
                  "sharedZones": {
                    "deck": [{"id": "red#1", "attributes": {}}, {"id": "red#2", "attributes": {}}],
                    "discard": [{"id": "blue#1", "attributes": {}}]
                  },
                  "perPlayerZones": {
                    "hand": {
                      "alice": [{"id": "a1", "attributes": {}}],
                      "bob": [{"id": "b1", "attributes": {}}, {"id": "b2", "attributes": {}}]
                    }
                  }
                }
                """);
    }

    @Test
    void sharedZoneDefaultsToPublic_whenNotDeclared() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), null, Map.of());

        assertThat(result.get("sharedZones").get("deck").isArray()).isTrue();
        assertThat(result.get("sharedZones").get("deck")).hasSize(2);
    }

    @Test
    void sharedZoneHidden_becomesHiddenCountForEveryone() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), "alice", Map.of("deck", ZoneVisibility.HIDDEN));

        assertThat(result.get("sharedZones").get("deck").has("hiddenCount")).isTrue();
        assertThat(result.get("sharedZones").get("deck").get("hiddenCount").asInt()).isEqualTo(2);
        // discard no está en el mapa -> PUBLIC por defecto, no se toca.
        assertThat(result.get("sharedZones").get("discard").isArray()).isTrue();
    }

    @Test
    void perPlayerZoneDefaultsToOwnerOnly_whenNotDeclared() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), "alice", Map.of());

        assertThat(result.get("perPlayerZones").get("hand").get("alice").isArray()).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").has("hiddenCount")).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").get("hiddenCount").asInt()).isEqualTo(2);
    }

    @Test
    void perPlayerZoneOwnerOnly_hidesFromSpectator() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), null, Map.of("hand", ZoneVisibility.OWNER_ONLY));

        assertThat(result.get("perPlayerZones").get("hand").get("alice").has("hiddenCount")).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").has("hiddenCount")).isTrue();
    }

    @Test
    void perPlayerZoneHidden_hidesEvenFromOwner() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), "alice", Map.of("hand", ZoneVisibility.HIDDEN));

        assertThat(result.get("perPlayerZones").get("hand").get("alice").has("hiddenCount")).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").has("hiddenCount")).isTrue();
    }

    @Test
    void perPlayerZoneAllButOwner_hidesOnlyFromOwner() throws Exception {
        JsonNode result =
                GameStateVisibility.forViewer(fixture(), "alice", Map.of("hand", ZoneVisibility.ALL_BUT_OWNER));

        assertThat(result.get("perPlayerZones").get("hand").get("alice").has("hiddenCount")).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").isArray()).isTrue();
    }

    @Test
    void perPlayerZonePublic_isVisibleToEveryoneIncludingOwner() throws Exception {
        JsonNode result = GameStateVisibility.forViewer(fixture(), null, Map.of("hand", ZoneVisibility.PUBLIC));

        assertThat(result.get("perPlayerZones").get("hand").get("alice").isArray()).isTrue();
        assertThat(result.get("perPlayerZones").get("hand").get("bob").isArray()).isTrue();
    }
}
