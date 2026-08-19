package com.motorcardgame.engine.rule;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.condition.AndCondition;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de punta a punta del ciclo EVENT → CONDITION → TARGET → ACTION con un juego de juguete
 * (no UNO real): cuando empieza el turno, si la pila compartida tiene cartas, el jugador actual
 * roba la carta superior.
 */
class RuleEngineTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void currentPlayerDrawsTopCardWhenPileHasCards() {
        GameState state = newStateWithPileOf(3);
        RuleEngine engine = newDrawTopCardEngine();
        Card expectedTopCard = ((LinearZone) state.sharedZone("pile")).peekTop();

        assertTrue(engine.handle(Event.of("TURN_STARTED"), state));

        assertEquals(2, ((LinearZone) state.sharedZone("pile")).size());
        LinearZone aliceHand = (LinearZone) state.zoneOf(ALICE.id(), "hand");
        assertEquals(1, aliceHand.size());
        assertEquals(expectedTopCard, aliceHand.peekTop());
        assertTrue(((LinearZone) state.zoneOf(BOB.id(), "hand")).isEmpty());
    }

    @Test
    void doesNothingWhenPileIsEmpty() {
        GameState state = newStateWithPileOf(0);
        RuleEngine engine = newDrawTopCardEngine();

        assertFalse(engine.handle(Event.of("TURN_STARTED"), state));

        assertTrue(((LinearZone) state.zoneOf(ALICE.id(), "hand")).isEmpty());
        assertTrue(((LinearZone) state.zoneOf(BOB.id(), "hand")).isEmpty());
    }

    @Test
    void doesNothingWhenEventTypeDoesNotMatch() {
        GameState state = newStateWithPileOf(3);
        RuleEngine engine = newDrawTopCardEngine();

        assertFalse(engine.handle(Event.of("OTHER_EVENT"), state));

        assertEquals(3, ((LinearZone) state.sharedZone("pile")).size());
        assertTrue(((LinearZone) state.zoneOf(ALICE.id(), "hand")).isEmpty());
    }

    @Test
    void executesActionOncePerResolvedTarget() {
        GameState state = new GameState(List.of(ALICE, BOB));
        int[] executions = {0};
        Rule rule = new Rule(
                "TICK",
                new AndCondition(),
                ctx -> List.of(ALICE, BOB),
                ctx -> executions[0]++);
        RuleEngine engine = new RuleEngine(List.of(rule));

        assertTrue(engine.handle(Event.of("TICK"), state));

        assertEquals(2, executions[0]);
    }

    @Test
    void cardScopedRuleOnlyFiresForEventsReferencingThatCardTemplate() {
        GameState state = new GameState(List.of(ALICE, BOB));
        int[] executions = {0};
        Rule rule = new Rule(
                "CARD_PLAYED",
                new AndCondition(),
                ctx -> List.of(ALICE),
                ctx -> executions[0]++,
                "reverso-rojo");
        RuleEngine engine = new RuleEngine(List.of(rule));

        assertFalse(engine.handle(Event.of("CARD_PLAYED"), state), "sin payload no puede identificar la carta");
        assertFalse(engine.handle(new Event("CARD_PLAYED", Map.of("cardId", 42)), state), "payload no-String, no ClassCastException");
        assertFalse(engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "wild#1")), state), "carta distinta");
        assertEquals(0, executions[0]);

        assertTrue(engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "reverso-rojo#1")), state));
        assertEquals(1, executions[0]);
    }

    @Test
    void unscopedRuleFiresRegardlessOfCardIdPayload() {
        GameState state = new GameState(List.of(ALICE, BOB));
        int[] executions = {0};
        Rule rule = new Rule("CARD_PLAYED", new AndCondition(), ctx -> List.of(ALICE), ctx -> executions[0]++);
        RuleEngine engine = new RuleEngine(List.of(rule));

        assertTrue(engine.handle(Event.of("CARD_PLAYED"), state));
        assertTrue(engine.handle(new Event("CARD_PLAYED", Map.of("cardId", "whatever#1")), state));
        assertEquals(2, executions[0]);
    }

    private static GameState newStateWithPileOf(int cardCount) {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone pile = new LinearZone();
        for (int i = cardCount; i >= 1; i--) {
            pile.pushTop(new Card(new CardId("c" + i), Map.of()));
        }
        state.registerSharedZone("pile", pile);
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        state.registerPlayerZone(BOB.id(), "hand", new LinearZone());
        return state;
    }

    private static RuleEngine newDrawTopCardEngine() {
        Condition pileHasCards = ctx -> !((LinearZone) ctx.gameState().sharedZone("pile")).isEmpty();
        Target currentPlayer = ctx -> List.of(ctx.gameState().currentPlayer());
        Action drawTopCard = ctx -> {
            Player player = (Player) ctx.target().orElseThrow();
            LinearZone pile = (LinearZone) ctx.gameState().sharedZone("pile");
            LinearZone hand = (LinearZone) ctx.gameState().zoneOf(player.id(), "hand");
            hand.pushTop(pile.popTop());
        };

        Rule rule = new Rule("TURN_STARTED", pileHasCards, currentPlayer, drawTopCard);

        return new RuleEngine(List.of(rule));
    }
}
