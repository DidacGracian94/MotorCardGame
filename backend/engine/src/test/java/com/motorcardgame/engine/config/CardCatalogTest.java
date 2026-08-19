package com.motorcardgame.engine.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Card;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardCatalogTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void generatesUniqueIdsForRepeatedCards() throws Exception {
        JsonNode cardsNode = readTree("""
                [
                  { "id": "red-1", "zone": "deck", "count": 2, "attributes": { "color": "RED", "value": 1 } }
                ]
                """);

        Map<String, List<Card>> byZone = CardCatalog.parseByZone(cardsNode);

        List<Card> deckCards = byZone.get("deck");
        assertEquals(2, deckCards.size());
        assertEquals("red-1#1", deckCards.get(0).id().value());
        assertEquals("red-1#2", deckCards.get(1).id().value());
        assertEquals("RED", deckCards.get(0).attribute("color"));
        assertEquals(1, deckCards.get(0).attribute("value"));
    }

    @Test
    void defaultsCountToOne() throws Exception {
        JsonNode cardsNode = readTree("""
                [ { "id": "wild", "zone": "deck", "attributes": {} } ]
                """);

        List<Card> deckCards = CardCatalog.parseByZone(cardsNode).get("deck");

        assertEquals(1, deckCards.size());
        assertEquals("wild#1", deckCards.get(0).id().value());
    }

    @Test
    void groupsCardsByDestinationZone() throws Exception {
        JsonNode cardsNode = readTree("""
                [
                  { "id": "a", "zone": "deck", "attributes": {} },
                  { "id": "b", "zone": "board", "attributes": {} }
                ]
                """);

        Map<String, List<Card>> byZone = CardCatalog.parseByZone(cardsNode);

        assertEquals(1, byZone.get("deck").size());
        assertEquals(1, byZone.get("board").size());
    }

    @Test
    void missingCardsNodeReturnsEmptyMap() {
        assertTrue(CardCatalog.parseByZone(MissingNode.getInstance()).isEmpty());
    }

    @Test
    void nonArrayCardsNodeIsInvalid() throws Exception {
        JsonNode cardsNode = readTree("{}");

        assertThrows(InvalidGameDefinitionException.class, () -> CardCatalog.parseByZone(cardsNode));
    }

    @Test
    void duplicateTemplateIdIsInvalid() throws Exception {
        JsonNode cardsNode = readTree("""
                [
                  { "id": "red-1", "zone": "deck", "attributes": {} },
                  { "id": "red-1", "zone": "discard", "attributes": {} }
                ]
                """);

        assertThrows(InvalidGameDefinitionException.class, () -> CardCatalog.parseByZone(cardsNode));
    }

    @Test
    void templateIdContainingHashIsInvalid() throws Exception {
        JsonNode cardsNode = readTree("""
                [ { "id": "wild#4", "zone": "deck", "attributes": {} } ]
                """);

        assertThrows(InvalidGameDefinitionException.class, () -> CardCatalog.parseByZone(cardsNode));
    }

    private static JsonNode readTree(String json) throws Exception {
        return OBJECT_MAPPER.readTree(json);
    }
}
