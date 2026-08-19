package com.motorcardgame.engine.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public static int optionalInt(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }
        if (!value.isIntegralNumber()) {
            throw new InvalidGameDefinitionException("field \"" + field + "\" must be an integer: " + node);
        }
        return value.asInt();
    }

    public static String optionalText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }
        if (!value.isTextual()) {
            throw new InvalidGameDefinitionException("field \"" + field + "\" must be text: " + node);
        }
        return value.asText();
    }

    /**
     * Convierte un nodo escalar (texto, booleano o entero) a su equivalente Java. Lo usan tanto
     * la lectura de {@code CARD_ATTRIBUTE_EQUALS} como la de atributos de carta en
     * {@code CardCatalog}/{@code GameStateSerializer} — el mismo conjunto reducido de tipos vale
     * para ambos, ninguno necesita más que texto/booleano/entero.
     */
    public static Object scalarValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isIntegralNumber()) {
            return node.asInt();
        }
        throw new InvalidGameDefinitionException("unsupported value, expected text, boolean or integer: " + node);
    }

    public static void putScalar(ObjectNode node, String field, Object value) {
        if (value instanceof String text) {
            node.put(field, text);
        } else if (value instanceof Boolean bool) {
            node.put(field, bool);
        } else if (value instanceof Integer integer) {
            node.put(field, integer);
        } else {
            throw new IllegalArgumentException("unsupported attribute value type for \"" + field + "\": " + value);
        }
    }
}
