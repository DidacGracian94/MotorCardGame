package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.motorcardgame.engine.state.ZoneVisibility;
import java.util.Map;

/**
 * Filtra un {@code GameState} serializado según la {@link ZoneVisibility} declarada para cada
 * zona (independiente de quién es su dueño — ver la nota en {@link ZoneVisibility}). Sustituye,
 * tanto en {@code sharedZones} como en {@code perPlayerZones}, cada zona que {@code viewerPlayerId}
 * no puede ver por {@code { "hiddenCount": N }} (objeto, no un array ni un número desnudo) para no
 * confundir "zona vacía y visible" ({@code []}) con "zona vacía y oculta".
 */
public final class GameStateVisibility {

    private GameStateVisibility() {}

    public static JsonNode forViewer(
            JsonNode fullState, String viewerPlayerId, Map<String, ZoneVisibility> zoneVisibilityByName) {
        ObjectNode result = fullState.deepCopy();

        ObjectNode sharedZones = (ObjectNode) result.get("sharedZones");
        for (Map.Entry<String, JsonNode> zoneEntry : sharedZones.properties()) {
            ZoneVisibility visibility = zoneVisibilityByName.getOrDefault(zoneEntry.getKey(), ZoneVisibility.PUBLIC);
            if (visibility == ZoneVisibility.HIDDEN) {
                sharedZones.set(zoneEntry.getKey(), hidden(sharedZones, zoneEntry.getValue().size()));
            }
        }

        ObjectNode perPlayerZones = (ObjectNode) result.get("perPlayerZones");
        for (Map.Entry<String, JsonNode> zoneEntry : perPlayerZones.properties()) {
            ZoneVisibility visibility =
                    zoneVisibilityByName.getOrDefault(zoneEntry.getKey(), ZoneVisibility.OWNER_ONLY);
            ObjectNode zoneByOwner = (ObjectNode) zoneEntry.getValue();
            for (Map.Entry<String, JsonNode> ownerEntry : zoneByOwner.properties()) {
                boolean isOwner = ownerEntry.getKey().equals(viewerPlayerId);
                if (!isVisibleTo(visibility, isOwner)) {
                    zoneByOwner.set(ownerEntry.getKey(), hidden(zoneByOwner, ownerEntry.getValue().size()));
                }
            }
        }

        return result;
    }

    private static boolean isVisibleTo(ZoneVisibility visibility, boolean isOwner) {
        return switch (visibility) {
            case PUBLIC -> true;
            case HIDDEN -> false;
            case OWNER_ONLY -> isOwner;
            case ALL_BUT_OWNER -> !isOwner;
        };
    }

    private static ObjectNode hidden(ContainerNode<?> nodeFactorySource, int size) {
        ObjectNode hidden = nodeFactorySource.objectNode();
        hidden.put("hiddenCount", size);
        return hidden;
    }
}
