package com.motorcardgame.engine.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parsea el catálogo de cartas de una config ({@code "cards"}): una lista de plantillas
 * (id/zona destino/cantidad/atributos), no de instancias — genérico para cualquier juego con
 * cartas repetidas (varias copias del mismo valor), no específico de UNO.
 */
public final class CardCatalog {

    private CardCatalog() {
    }

    /**
     * Agrupa las cartas generadas por su zona destino. Con {@code count > 1} se generan copias
     * con ids únicos ({@code id#1}, {@code id#2}, ...) — la identidad de {@link Card} es por
     * {@link CardId}, así que dos copias con los mismos atributos igualmente necesitan ids
     * distintos.
     * <p>
     * El id de cada plantilla debe ser único en todo {@code "cards"} y no puede contener
     * {@code '#'} — {@link CardId#templateId()} corta en el primer {@code '#'} para recuperar el
     * id de plantilla que originó una carta (lo usa {@code RuleEngine} para dar ámbito a las
     * reglas de {@code cards[].rules}), y esa operación dejaría de ser fiable con cualquiera de
     * las dos cosas.
     */
    public static Map<String, List<Card>> parseByZone(JsonNode cardsNode) {
        Map<String, List<Card>> byZone = new LinkedHashMap<>();
        if (cardsNode.isMissingNode() || cardsNode.isNull()) {
            return byZone;
        }
        if (!cardsNode.isArray()) {
            throw new InvalidGameDefinitionException("\"cards\" must be an array: " + cardsNode);
        }
        Set<String> seenTemplateIds = new HashSet<>();
        for (JsonNode cardNode : cardsNode) {
            String templateId = JsonNodes.requiredText(cardNode, "id");
            if (templateId.contains("#")) {
                throw new InvalidGameDefinitionException("card template id must not contain \"#\": " + templateId);
            }
            if (!seenTemplateIds.add(templateId)) {
                throw new InvalidGameDefinitionException("duplicate card template id: " + templateId);
            }
            String zone = JsonNodes.requiredText(cardNode, "zone");
            int count = JsonNodes.optionalInt(cardNode, "count", 1);
            Map<String, Object> attributes = readAttributes(cardNode.path("attributes"));

            List<Card> cardsForZone = byZone.computeIfAbsent(zone, key -> new ArrayList<>());
            for (int i = 1; i <= count; i++) {
                cardsForZone.add(new Card(new CardId(templateId + "#" + i), attributes));
            }
        }
        return byZone;
    }

    public static Map<String, Object> readAttributes(JsonNode attributesNode) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (attributesNode == null || attributesNode.isMissingNode() || attributesNode.isNull()) {
            return attributes;
        }
        for (Map.Entry<String, JsonNode> field : attributesNode.properties()) {
            attributes.put(field.getKey(), JsonNodes.scalarValue(field.getValue()));
        }
        return attributes;
    }
}
