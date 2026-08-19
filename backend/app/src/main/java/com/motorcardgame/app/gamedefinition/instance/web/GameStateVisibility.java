package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

/**
 * Filtra un {@code GameState} serializado para que la mano de un jugador nunca viaje a nadie más
 * que él mismo. Sustituye, en {@code perPlayerZones}, cada zona de un jugador distinto de
 * {@code viewerPlayerId} por {@code { "hiddenCount": N }} (objeto, no un array ni un número
 * desnudo) para no confundir "mano vacía y visible" ({@code []}) con "mano vacía y oculta".
 * {@code sharedZones} no se toca: el orden del mazo/descarte es visible para todos.
 */
public final class GameStateVisibility {

    private GameStateVisibility() {}

    public static JsonNode forViewer(JsonNode fullState, String viewerPlayerId) {
        ObjectNode result = fullState.deepCopy();
        ObjectNode perPlayerZones = (ObjectNode) result.get("perPlayerZones");
        for (Map.Entry<String, JsonNode> zoneEntry : perPlayerZones.properties()) {
            ObjectNode zoneByOwner = (ObjectNode) zoneEntry.getValue();
            for (Map.Entry<String, JsonNode> ownerEntry : zoneByOwner.properties()) {
                String ownerPlayerId = ownerEntry.getKey();
                if (ownerPlayerId.equals(viewerPlayerId)) {
                    continue;
                }
                ObjectNode hidden = zoneByOwner.objectNode();
                hidden.put("hiddenCount", ownerEntry.getValue().size());
                zoneByOwner.set(ownerPlayerId, hidden);
            }
        }
        return result;
    }
}
