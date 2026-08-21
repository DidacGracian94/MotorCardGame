package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Ownership;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import com.motorcardgame.engine.state.ZoneRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WinningCardOwnerTargetTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    private static final WinningCardOwnerTarget TARGET = new WinningCardOwnerTarget(
            new ZoneRef("mesa", Ownership.SHARED),
            "strength",
            "suit",
            new ZoneRef("triunfo", Ownership.SHARED),
            Position.TOP,
            "playedBy");

    @Test
    void trumpCardWinsOverStrongerNonTrumpCard() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone mesa = new LinearZone();
        // Alice led with "espadas" (not trump); Bob followed with "oros" (trump), weaker rank.
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "espadas", "strength", 2, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "oros", "strength", 1, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        LinearZone triunfo = new LinearZone();
        triunfo.pushTop(new Card(new CardId("trump"), Map.of("suit", "oros")));
        state.registerSharedZone("triunfo", triunfo);

        List<Object> resolved = TARGET.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(BOB), resolved);
    }

    @Test
    void highestTrumpWinsAmongSeveralTrumpCards() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone mesa = new LinearZone();
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "oros", "strength", 3, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "oros", "strength", 10, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        LinearZone triunfo = new LinearZone();
        triunfo.pushTop(new Card(new CardId("trump"), Map.of("suit", "oros")));
        state.registerSharedZone("triunfo", triunfo);

        List<Object> resolved = TARGET.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(BOB), resolved);
    }

    @Test
    void fallsBackToLedSuitWhenNoCardMatchesTrump() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone mesa = new LinearZone();
        // Alice led with "espadas"; Bob's stronger "copas" card is neither trump nor led suit.
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "espadas", "strength", 2, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "copas", "strength", 8, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        LinearZone triunfo = new LinearZone();
        triunfo.pushTop(new Card(new CardId("trump"), Map.of("suit", "oros")));
        state.registerSharedZone("triunfo", triunfo);

        List<Object> resolved = TARGET.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(ALICE), resolved);
    }

    @Test
    void fallsBackToLedSuitWhenPreferredGroupZoneIsEmpty() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone mesa = new LinearZone();
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "bastos", "strength", 5, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "bastos", "strength", 9, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        state.registerSharedZone("triunfo", new LinearZone());

        List<Object> resolved = TARGET.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(BOB), resolved);
    }

    @Test
    void resolvesTrumpCardFromRememberedVariableWithoutAnyZone() {
        GameState state = new GameState(List.of(ALICE, BOB));
        state.setVariable("trumpSuit", "oros");
        LinearZone mesa = new LinearZone();
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "espadas", "strength", 2, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "oros", "strength", 1, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        WinningCardOwnerTarget target = new WinningCardOwnerTarget(
                new ZoneRef("mesa", Ownership.SHARED), "strength", "suit", null, Position.TOP, "playedBy", "trumpSuit");

        List<Object> resolved = target.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(BOB), resolved);
    }

    @Test
    void stillRecognizesTrumpFromVariableAfterTheRevealingCardHasLeftItsZone() {
        // Regression test for the real bug this capability fixes: once the trump-revealing card is
        // drawn away from "triunfo" (normal late-game replenishment), the zone-based lookup used to
        // go blind to trump for the rest of the game. Remembering the suit as a variable at reveal
        // time keeps it recognized regardless of where that physical card ends up afterwards.
        GameState state = new GameState(List.of(ALICE, BOB));
        state.setVariable("trumpSuit", "oros");
        state.registerSharedZone("triunfo", new LinearZone()); // the reveal card has since been drawn away
        LinearZone mesa = new LinearZone();
        mesa.pushBottom(new Card(new CardId("led"), Map.of("suit", "espadas", "strength", 9, "playedBy", "alice")));
        mesa.pushTop(new Card(new CardId("follow"), Map.of("suit", "oros", "strength", 1, "playedBy", "bob")));
        state.registerSharedZone("mesa", mesa);
        WinningCardOwnerTarget target = new WinningCardOwnerTarget(
                new ZoneRef("mesa", Ownership.SHARED), "strength", "suit", null, Position.TOP, "playedBy", "trumpSuit");

        List<Object> resolved = target.resolve(new RuleContext(state, Event.of("PLAY_CARD")));

        assertEquals(List.of(BOB), resolved, "Bob's weaker trump card should still beat Alice's stronger led-suit card");
    }

    @Test
    void constructorRequiresEitherPreferredGroupZoneOrVariable() {
        assertThrows(IllegalArgumentException.class, () -> new WinningCardOwnerTarget(
                new ZoneRef("mesa", Ownership.SHARED), "strength", "suit", null, Position.TOP, "playedBy", null));
    }
}
