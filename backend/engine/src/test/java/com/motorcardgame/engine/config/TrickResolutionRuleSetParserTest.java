package com.motorcardgame.engine.config;

import com.motorcardgame.engine.event.Event;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de punta a punta de la resolución de una baza (mover a la mesa, sumar puntos, ceder el
 * turno al ganador, recoger las cartas), encadenando las capacidades nuevas
 * ({@code ZONE_CARD_COUNT_EQUALS}, {@code WINNING_CARD_OWNER}, {@code ADD_POINTS},
 * {@code SET_CURRENT_PLAYER}, {@code MOVE_ALL_CARDS}, {@code MOVE_CARD}'s {@code stampOwnerAs})
 * como una sola secuencia de reglas sobre el mismo evento {@code PLAY_CARD} — no cada capacidad
 * por separado (eso ya lo cubre su propio test unitario), sino que la cadena completa funciona
 * junta, igual que necesitaría un juego de baza real (Brisca, Tute, ...) sin que ninguna de estas
 * capacidades mencione ese juego.
 */
class TrickResolutionRuleSetParserTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    private static final String CONFIG = """
            {
              "rules": [
                {
                  "event": "PLAY_CARD",
                  "condition": { "type": "AND", "conditions": [] },
                  "target": { "type": "CURRENT_PLAYER" },
                  "action": {
                    "type": "MOVE_CARD",
                    "from": { "name": "hand", "ownership": "PER_PLAYER" },
                    "to": { "name": "mesa", "ownership": "SHARED" },
                    "stampOwnerAs": "playedBy"
                  }
                },
                {
                  "event": "PLAY_CARD",
                  "condition": {
                    "type": "NOT",
                    "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 }
                  },
                  "target": { "type": "CURRENT_PLAYER" },
                  "action": { "type": "NEXT_PLAYER" }
                },
                {
                  "event": "PLAY_CARD",
                  "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
                  "target": {
                    "type": "WINNING_CARD_OWNER",
                    "zone": { "name": "mesa", "ownership": "SHARED" },
                    "rankAttribute": "strength",
                    "groupAttribute": "suit",
                    "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
                    "ownerAttribute": "playedBy"
                  },
                  "action": { "type": "ADD_POINTS", "zone": { "name": "mesa", "ownership": "SHARED" }, "attribute": "points" }
                },
                {
                  "event": "PLAY_CARD",
                  "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
                  "target": {
                    "type": "WINNING_CARD_OWNER",
                    "zone": { "name": "mesa", "ownership": "SHARED" },
                    "rankAttribute": "strength",
                    "groupAttribute": "suit",
                    "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
                    "ownerAttribute": "playedBy"
                  },
                  "action": { "type": "SET_CURRENT_PLAYER" }
                },
                {
                  "event": "PLAY_CARD",
                  "condition": { "type": "ZONE_CARD_COUNT_EQUALS", "zone": { "name": "mesa", "ownership": "SHARED" }, "count": 2 },
                  "target": {
                    "type": "WINNING_CARD_OWNER",
                    "zone": { "name": "mesa", "ownership": "SHARED" },
                    "rankAttribute": "strength",
                    "groupAttribute": "suit",
                    "preferredGroupZone": { "name": "triunfo", "ownership": "SHARED" },
                    "ownerAttribute": "playedBy"
                  },
                  "action": {
                    "type": "MOVE_ALL_CARDS",
                    "from": { "name": "mesa", "ownership": "SHARED" },
                    "to": { "name": "bazas", "ownership": "PER_PLAYER" }
                  }
                }
              ]
            }
            """;

    @Test
    void resolvesTrickAcrossTwoPlayedCards() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone aliceHand = new LinearZone();
        aliceHand.pushTop(new Card(new CardId("three-of-oros"), Map.of("suit", "espadas", "strength", 9, "points", 10)));
        state.registerPlayerZone(ALICE.id(), "hand", aliceHand);
        LinearZone bobHand = new LinearZone();
        bobHand.pushTop(new Card(new CardId("ace-of-oros"), Map.of("suit", "oros", "strength", 10, "points", 11)));
        state.registerPlayerZone(BOB.id(), "hand", bobHand);
        state.registerSharedZone("mesa", new LinearZone());
        LinearZone triunfo = new LinearZone();
        triunfo.pushTop(new Card(new CardId("trump-marker"), Map.of("suit", "oros")));
        state.registerSharedZone("triunfo", triunfo);
        state.registerPlayerZone(ALICE.id(), "bazas", new LinearZone());
        state.registerPlayerZone(BOB.id(), "bazas", new LinearZone());

        RuleEngine engine = engineFor(CONFIG);

        // Alice (current player) leads with a non-trump card.
        boolean firstMatched = engine.handle(new Event("PLAY_CARD", Map.of("cardId", "three-of-oros")), state);
        assertTrue(firstMatched);
        assertEquals(BOB, state.currentPlayer());
        assertEquals(1, ((LinearZone) state.sharedZone("mesa")).size());

        // Bob follows with the trump card and should win the trick.
        boolean secondMatched = engine.handle(new Event("PLAY_CARD", Map.of("cardId", "ace-of-oros")), state);
        assertTrue(secondMatched);

        assertEquals(BOB, state.currentPlayer());
        assertEquals(21, BOB.score());
        assertEquals(0, ALICE.score());
        assertTrue(((LinearZone) state.sharedZone("mesa")).isEmpty());
        assertEquals(2, ((LinearZone) state.zoneOf(BOB.id(), "bazas")).size());
        assertEquals(0, ((LinearZone) state.zoneOf(ALICE.id(), "bazas")).size());
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
}
