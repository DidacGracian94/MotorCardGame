package com.motorcardgame.engine.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;

/**
 * Lectura defensiva de campos de un {@link JsonNode} de configuración: cada método valida
 * presencia y tipo, y traduce cualquier problema a {@link InvalidGameDefinitionException} con un
 * mensaje que apunta al campo y al nodo — la configuración de un juego la escribe quien diseña el
 * juego, no quien mantiene el motor, así que el error debe ser legible sin leer código.
 */
public final class JsonNodes {

    private JsonNodes() {
    }

    public static String requiredText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isTextual()) {
            throw new InvalidGameDefinitionException("missing required text field \"" + field + "\": " + node);
        }
        return value.asText();
    }

    public static int requiredInt(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isIntegralNumber()) {
            throw new InvalidGameDefinitionException("missing required integer field \"" + field + "\": " + node);
        }
        return value.asInt();
    }

    public static JsonNode requiredObject(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isObject()) {
            throw new InvalidGameDefinitionException("missing required object field \"" + field + "\": " + node);
        }
        return value;
    }

    public static JsonNode requiredArray(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isArray()) {
            throw new InvalidGameDefinitionException("missing required array field \"" + field + "\": " + node);
        }
        return value;
    }
}
