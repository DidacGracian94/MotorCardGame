package com.motorcardgame.engine.config;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.rule.Rule;
import com.motorcardgame.engine.rule.RuleEngine;
import com.motorcardgame.engine.rule.registry.ActionRegistry;
import com.motorcardgame.engine.rule.registry.ConditionRegistry;
import com.motorcardgame.engine.rule.registry.StandardCapabilities;
import com.motorcardgame.engine.rule.registry.TargetRegistry;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de punta a punta: JSON de configuración → {@link RuleSetParser} → {@link Rule} reales →
 * {@link RuleEngine} ejecutando sobre un {@link GameState}. Cubre el mismo escenario que
 * {@code RuleEngineTest} (robar del mazo si no está vacío) pero construido enteramente a partir
 * de JSON y las capacidades estándar de {@link StandardCapabilities}, más composición AND/OR/NOT/
 * SEQUENCE/REPEAT y los casos de error.
 */
class RuleSetParserTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void currentPlayerDrawsTopCardWhenPileHasCards() {
        GameState state = newStateWithPile(3, Map.of());
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "TURN_STARTED",
                      "condition": {
                        "type": "NOT",
                        "condition": { "type": "ZONE_IS_EMPTY", "zone": { "name": "pile", "ownership": "SHARED" } }
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "DRAW_CARDS",
                        "count": 1,
                        "from": { "name": "pile", "ownership": "SHARED" },
                        "to": { "name": "hand", "ownership": "PER_PLAYER" }
                      }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("TURN_STARTED"), state);

        assertEquals(2, ((LinearZone) state.sharedZone("pile")).size());
        assertEquals(1, ((LinearZone) state.zoneOf(ALICE.id(), "hand")).size());
    }

    @Test
    void doesNothingWhenPileIsEmpty() {
        GameState state = newStateWithPile(0, Map.of());
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "TURN_STARTED",
                      "condition": {
                        "type": "NOT",
                        "condition": { "type": "ZONE_IS_EMPTY", "zone": { "name": "pile", "ownership": "SHARED" } }
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "DRAW_CARDS",
                        "count": 1,
                        "from": { "name": "pile", "ownership": "SHARED" },
                        "to": { "name": "hand", "ownership": "PER_PLAYER" }
                      }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("TURN_STARTED"), state);

        assertEquals(0, ((LinearZone) state.zoneOf(ALICE.id(), "hand")).size());
    }

    @Test
    void andRequiresAllSubConditionsTrue() {
        GameState state = newStateWithPile(1, Map.of("color", "RED"));
        RuleEngine engine = engineFor(andRule());

        engine.handle(Event.of("CARD_PLAYED"), state);

        assertEquals(BOB, state.currentPlayer());
    }

    @Test
    void andDoesNothingWhenOneSubConditionIsFalse() {
        GameState state = newStateWithPile(1, Map.of("color", "BLUE"));
        RuleEngine engine = engineFor(andRule());

        engine.handle(Event.of("CARD_PLAYED"), state);

        assertEquals(ALICE, state.currentPlayer());
    }

    @Test
    void orFiresWhenAtLeastOneSubConditionIsTrue() {
        GameState state = newStateWithPile(1, Map.of("color", "BLUE"));
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "OR",
                        "conditions": [
                          { "type": "CARD_ATTRIBUTE_EQUALS", "zone": { "name": "pile", "ownership": "SHARED" }, "attribute": "color", "equals": "RED" },
                          { "type": "CARD_ATTRIBUTE_EQUALS", "zone": { "name": "pile", "ownership": "SHARED" }, "attribute": "color", "equals": "BLUE" }
                        ]
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("CARD_PLAYED"), state);

        assertEquals(BOB, state.currentPlayer());
    }

    @Test
    void sequenceExecutesActionsInOrder() {
        GameState state = newStateWithPile(1, Map.of());
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "TURN_STARTED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "SEQUENCE",
                        "actions": [
                          {
                            "type": "DRAW_CARDS", "count": 1,
                            "from": { "name": "pile", "ownership": "SHARED" },
                            "to": { "name": "hand", "ownership": "PER_PLAYER" }
                          },
                          { "type": "NEXT_PLAYER" }
                        ]
                      }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("TURN_STARTED"), state);

        assertEquals(1, ((LinearZone) state.zoneOf(ALICE.id(), "hand")).size());
        assertEquals(BOB, state.currentPlayer());
    }

    @Test
    void repeatExecutesActionGivenTimes() {
        GameState state = newStateWithPile(2, Map.of());
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "TURN_STARTED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "REPEAT",
                        "times": 2,
                        "action": {
                          "type": "DRAW_CARDS", "count": 1,
                          "from": { "name": "pile", "ownership": "SHARED" },
                          "to": { "name": "hand", "ownership": "PER_PLAYER" }
                        }
                      }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("TURN_STARTED"), state);

        assertEquals(2, ((LinearZone) state.zoneOf(ALICE.id(), "hand")).size());
    }

    @Test
    void moveCardMovesNamedCardWhenAttributeMatchesZone() {
        GameState state = newStateWithPile(1, Map.of("color", "RED"));
        LinearZone aliceHand = (LinearZone) state.zoneOf(ALICE.id(), "hand");
        aliceHand.pushTop(new Card(new CardId("played"), Map.of("color", "RED")));
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "CARD_ATTRIBUTE_MATCHES_ZONE",
                        "cardZone": { "name": "hand", "ownership": "PER_PLAYER" },
                        "attribute": "color",
                        "zone": { "name": "pile", "ownership": "SHARED" },
                        "position": "TOP"
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "MOVE_CARD",
                        "from": { "name": "hand", "ownership": "PER_PLAYER" },
                        "to": { "name": "pile", "ownership": "SHARED" }
                      }
                    }
                  ]
                }
                """);

        boolean matched = engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "played")), state);

        assertTrue(matched);
        assertEquals(0, aliceHand.size());
        assertEquals("played", ((LinearZone) state.sharedZone("pile")).peekTop().id().value());
    }

    @Test
    void moveCardDoesNothingWhenAttributeDoesNotMatchZone() {
        GameState state = newStateWithPile(1, Map.of("color", "BLUE"));
        LinearZone aliceHand = (LinearZone) state.zoneOf(ALICE.id(), "hand");
        aliceHand.pushTop(new Card(new CardId("played"), Map.of("color", "RED")));
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "CARD_ATTRIBUTE_MATCHES_ZONE",
                        "cardZone": { "name": "hand", "ownership": "PER_PLAYER" },
                        "attribute": "color",
                        "zone": { "name": "pile", "ownership": "SHARED" },
                        "position": "TOP"
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "MOVE_CARD",
                        "from": { "name": "hand", "ownership": "PER_PLAYER" },
                        "to": { "name": "pile", "ownership": "SHARED" }
                      }
                    }
                  ]
                }
                """);

        boolean matched = engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "played")), state);

        assertFalse(matched);
        assertEquals(1, aliceHand.size());
        assertEquals("c1", ((LinearZone) state.sharedZone("pile")).peekTop().id().value());
    }

    /**
     * El orden de las reglas importa: la primera identifica la carta jugada por su valor
     * (mientras sigue en la mano) y la mueve al descarte; la segunda ya no necesita volver a
     * localizarla por cardId — mira el tope del descarte, que la primera regla acaba de dejar ahí
     * — así puede castigar al siguiente jugador sin que le afecte que la primera regla haya
     * modificado la mano de quien jugó.
     */
    @Test
    void chupa2StyleCardMovesToDiscardAndMakesNextPlayerDrawAndSkipsTheirTurn() {
        Player carol = new Player(new PlayerId("carol"), "Carol");
        GameState state = new GameState(List.of(ALICE, BOB, carol));
        LinearZone deck = new LinearZone();
        for (int i = 1; i <= 5; i++) {
            deck.pushTop(new Card(new CardId("deck" + i), Map.of()));
        }
        state.registerSharedZone("deck", deck);
        state.registerSharedZone("discard", new LinearZone());
        LinearZone aliceHand = new LinearZone();
        aliceHand.pushTop(new Card(new CardId("special"), Map.of("type", "chupa2")));
        state.registerPlayerZone(ALICE.id(), "hand", aliceHand);
        state.registerPlayerZone(BOB.id(), "hand", new LinearZone());
        state.registerPlayerZone(carol.id(), "hand", new LinearZone());

        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "EVENT_CARD_ATTRIBUTE_EQUALS",
                        "cardZone": { "name": "hand", "ownership": "PER_PLAYER" },
                        "attribute": "type",
                        "equals": "chupa2"
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": {
                        "type": "MOVE_CARD",
                        "from": { "name": "hand", "ownership": "PER_PLAYER" },
                        "to": { "name": "discard", "ownership": "SHARED" }
                      }
                    },
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "CARD_ATTRIBUTE_EQUALS",
                        "zone": { "name": "discard", "ownership": "SHARED" },
                        "position": "TOP",
                        "attribute": "type",
                        "equals": "chupa2"
                      },
                      "target": { "type": "NEXT_PLAYER" },
                      "action": {
                        "type": "SEQUENCE",
                        "actions": [
                          {
                            "type": "DRAW_CARDS", "count": 2,
                            "from": { "name": "deck", "ownership": "SHARED" },
                            "to": { "name": "hand", "ownership": "PER_PLAYER" }
                          },
                          { "type": "NEXT_PLAYER" },
                          { "type": "NEXT_PLAYER" }
                        ]
                      }
                    }
                  ]
                }
                """);

        boolean matched = engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "special")), state);

        assertTrue(matched);
        assertEquals(0, aliceHand.size());
        assertEquals("special", ((LinearZone) state.sharedZone("discard")).peekTop().id().value());
        assertEquals(2, ((LinearZone) state.zoneOf(BOB.id(), "hand")).size());
        assertEquals(carol, state.currentPlayer());
    }

    @Test
    void reverseDirectionMakesNextPlayerTargetResolveBackwards() {
        Player carol = new Player(new PlayerId("carol"), "Carol");
        GameState state = new GameState(List.of(ALICE, BOB, carol));
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        state.registerPlayerZone(BOB.id(), "hand", new LinearZone());
        state.registerPlayerZone(carol.id(), "hand", new LinearZone());
        RuleEngine engine = engineFor("""
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "REVERSE_DIRECTION" }
                    }
                  ]
                }
                """);

        engine.handle(Event.of("CARD_PLAYED"), state);

        assertEquals(-1, state.direction());
        assertEquals(carol, state.nextPlayer());
    }

    @Test
    void parseIncludesRulesNestedInsideCards() {
        GameState state = newStateWithPile(1, Map.of());
        RuleEngine engine = engineFor("""
                {
                  "rules": [],
                  "cards": [
                    {
                      "id": "chupa2",
                      "zone": "pile",
                      "attributes": {},
                      "rules": [
                        {
                          "event": "CARD_PLAYED",
                          "condition": { "type": "AND", "conditions": [] },
                          "target": { "type": "CURRENT_PLAYER" },
                          "action": { "type": "NEXT_PLAYER" }
                        }
                      ]
                    }
                  ]
                }
                """);

        boolean matched = engine.handle(Event.of("CARD_PLAYED"), state);

        assertTrue(matched);
        assertEquals(BOB, state.currentPlayer());
    }

    @Test
    void malformedJsonIsInvalid() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().parse("{not json"));
    }

    @Test
    void missingRulesArrayIsInvalid() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().parse("{}"));
    }

    @Test
    void ruleMissingEventFieldIsInvalid() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().parse("""
                { "rules": [ { "condition": {}, "target": {}, "action": {} } ] }
                """));
    }

    @Test
    void unknownCapabilityTypeIsInvalid() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().parse("""
                {
                  "rules": [
                    {
                      "event": "TURN_STARTED",
                      "condition": { "type": "UNKNOWN_CONDITION" },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """));
    }

    @Test
    void validateEventsAcceptsGameStartedWithoutAnyDeclaredPlayerActions() {
        newParser().validateEvents("""
                {
                  "rules": [
                    {
                      "event": "GAME_STARTED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """);
    }

    @Test
    void validateEventsAcceptsRuleEventDeclaredInPlayerActions() {
        newParser().validateEvents("""
                {
                  "playerActions": ["CARD_PLAYED"],
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """);
    }

    @Test
    void validateEventsRejectsRuleEventNotDeclaredAndNotGameStarted() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().validateEvents("""
                {
                  "playerActions": ["DRAW"],
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": { "type": "AND", "conditions": [] },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """));
    }

    @Test
    void validateEventsRejectsUndeclaredEventInCardNestedRule() {
        assertThrows(InvalidGameDefinitionException.class, () -> newParser().validateEvents("""
                {
                  "rules": [],
                  "cards": [
                    {
                      "id": "chupa2",
                      "zone": "pile",
                      "attributes": {},
                      "rules": [
                        {
                          "event": "NO_DECLARADO",
                          "condition": { "type": "AND", "conditions": [] },
                          "target": { "type": "CURRENT_PLAYER" },
                          "action": { "type": "NEXT_PLAYER" }
                        }
                      ]
                    }
                  ]
                }
                """));
    }

    @Test
    void parsePlayerActionsReturnsDeclaredNames() {
        assertEquals(java.util.Set.of("CARD_PLAYED", "DRAW"), newParser().parsePlayerActions("""
                { "playerActions": ["CARD_PLAYED", "DRAW"], "rules": [] }
                """));
    }

    @Test
    void parsePlayerActionsReturnsEmptySetWhenFieldAbsent() {
        assertEquals(java.util.Set.of(), newParser().parsePlayerActions("{ \"rules\": [] }"));
    }

    private static String andRule() {
        return """
                {
                  "rules": [
                    {
                      "event": "CARD_PLAYED",
                      "condition": {
                        "type": "AND",
                        "conditions": [
                          { "type": "NOT", "condition": { "type": "ZONE_IS_EMPTY", "zone": { "name": "pile", "ownership": "SHARED" } } },
                          { "type": "CARD_ATTRIBUTE_EQUALS", "zone": { "name": "pile", "ownership": "SHARED" }, "attribute": "color", "equals": "RED" }
                        ]
                      },
                      "target": { "type": "CURRENT_PLAYER" },
                      "action": { "type": "NEXT_PLAYER" }
                    }
                  ]
                }
                """;
    }

    private static RuleEngine engineFor(String configJson) {
        List<Rule> rules = newParser().parse(configJson);
        return new RuleEngine(rules);
    }

    private static RuleSetParser newParser() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);
        return new RuleSetParser(actionRegistry, conditionRegistry, targetRegistry);
    }

    private static GameState newStateWithPile(int cardCount, Map<String, Object> topCardAttributes) {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone pile = new LinearZone();
        for (int i = cardCount; i >= 2; i--) {
            pile.pushTop(new Card(new CardId("c" + i), Map.of()));
        }
        if (cardCount >= 1) {
            pile.pushTop(new Card(new CardId("c1"), topCardAttributes));
        }
        state.registerSharedZone("pile", pile);
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        state.registerPlayerZone(BOB.id(), "hand", new LinearZone());
        return state;
    }
}
