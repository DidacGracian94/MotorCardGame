package com.motorcardgame.engine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import com.motorcardgame.engine.state.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Única vía de persistencia de un {@link GameState}: lo convierte a/desde el JSON que se guarda
 * en {@code game_instances.state}. Sin esto, por más que el resto del motor sepa construir un
 * estado inicial, no habría forma de recuperarlo entre peticiones.
 */
public final class GameStateSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(GameState state) {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode playersNode = root.putArray("players");
        for (Player player : state.players()) {
            ObjectNode playerNode = playersNode.addObject();
            playerNode.put("id", player.id().value());
            playerNode.put("displayName", player.displayName());
            playerNode.put("score", player.score());
        }
        root.put("currentPlayerIndex", state.currentPlayerIndex());
        root.put("direction", state.direction());

        ObjectNode sharedZonesNode = root.putObject("sharedZones");
        for (Map.Entry<String, Zone> entry : state.sharedZones().entrySet()) {
            sharedZonesNode.set(entry.getKey(), cardsArray(entry.getValue()));
        }

        ObjectNode perPlayerZonesNode = root.putObject("perPlayerZones");
        for (Map.Entry<String, Map<PlayerId, Zone>> entry : state.perPlayerZones().entrySet()) {
            ObjectNode zonesByOwner = perPlayerZonesNode.putObject(entry.getKey());
            for (Map.Entry<PlayerId, Zone> ownerEntry : entry.getValue().entrySet()) {
                zonesByOwner.set(ownerEntry.getKey().value(), cardsArray(ownerEntry.getValue()));
            }
        }

        ObjectNode variablesNode = root.putObject("variables");
        state.variables().forEach((key, value) -> JsonNodes.putScalar(variablesNode, key, value));

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize GameState", e);
        }
    }

    public GameState fromJson(String json) {
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new InvalidGameDefinitionException("persisted state is not valid JSON: " + e.getOriginalMessage(), e);
        }

        List<Player> players = new ArrayList<>();
        for (JsonNode playerNode : JsonNodes.requiredArray(root, "players")) {
            players.add(new Player(
                    new PlayerId(JsonNodes.requiredText(playerNode, "id")),
                    JsonNodes.requiredText(playerNode, "displayName"),
                    JsonNodes.optionalInt(playerNode, "score", 0)));
        }
        int direction = JsonNodes.optionalInt(root, "direction", 1);
        GameState state = new GameState(players, JsonNodes.requiredInt(root, "currentPlayerIndex"), direction);

        JsonNode sharedZonesNode = JsonNodes.requiredObject(root, "sharedZones");
        for (Map.Entry<String, JsonNode> entry : sharedZonesNode.properties()) {
            state.registerSharedZone(entry.getKey(), zoneFromArray(entry.getValue()));
        }

        JsonNode perPlayerZonesNode = JsonNodes.requiredObject(root, "perPlayerZones");
        for (Map.Entry<String, JsonNode> zoneEntry : perPlayerZonesNode.properties()) {
            for (Map.Entry<String, JsonNode> ownerEntry : zoneEntry.getValue().properties()) {
                state.registerPlayerZone(new PlayerId(ownerEntry.getKey()), zoneEntry.getKey(), zoneFromArray(ownerEntry.getValue()));
            }
        }

        JsonNode variablesNode = root.path("variables");
        if (variablesNode.isObject()) {
            for (Map.Entry<String, JsonNode> entry : variablesNode.properties()) {
                state.setVariable(entry.getKey(), JsonNodes.scalarValue(entry.getValue()));
            }
        }

        return state;
    }

    private static ArrayNode cardsArray(Zone zone) {
        ArrayNode array = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        for (Card card : ((LinearZone) zone).cardsView()) {
            ObjectNode cardNode = array.addObject();
            cardNode.put("id", card.id().value());
            ObjectNode attributesNode = cardNode.putObject("attributes");
            card.attributes().forEach((key, value) -> JsonNodes.putScalar(attributesNode, key, value));
        }
        return array;
    }

    private static LinearZone zoneFromArray(JsonNode arrayNode) {
        List<Card> cardsTopToBottom = new ArrayList<>();
        for (JsonNode cardNode : arrayNode) {
            cardsTopToBottom.add(new Card(
                    new CardId(JsonNodes.requiredText(cardNode, "id")),
                    CardCatalog.readAttributes(cardNode.path("attributes"))));
        }
        return LinearZone.ofTopToBottom(cardsTopToBottom);
    }
}
