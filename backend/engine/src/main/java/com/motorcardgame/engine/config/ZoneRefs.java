package com.motorcardgame.engine.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Ownership;
import com.motorcardgame.engine.state.ZoneRef;

/**
 * Traduce un nodo JSON {@code {"name": "...", "ownership": "SHARED"|"PER_PLAYER"}} a un
 * {@link ZoneRef}. Es la única vía por la que un {@link Ownership} entra al motor — el resto del
 * motor lo recibe ya como dato tipado.
 */
public final class ZoneRefs {

    private ZoneRefs() {
    }

    public static ZoneRef fromJson(JsonNode node) {
        String name = JsonNodes.requiredText(node, "name");
        String ownershipText = JsonNodes.requiredText(node, "ownership");
        try {
            return new ZoneRef(name, Ownership.valueOf(ownershipText));
        } catch (IllegalArgumentException e) {
            throw new InvalidGameDefinitionException("unknown zone ownership \"" + ownershipText + "\": " + node, e);
        }
    }
}
