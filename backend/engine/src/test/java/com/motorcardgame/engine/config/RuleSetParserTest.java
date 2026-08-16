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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
