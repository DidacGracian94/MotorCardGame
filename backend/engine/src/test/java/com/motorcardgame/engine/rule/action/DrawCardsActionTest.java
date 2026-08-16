package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
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

class DrawCardsActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void movesCardsFromSharedPileToPerPlayerHand() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c1"), Map.of()));
        pile.pushTop(new Card(new CardId("c2"), Map.of()));
        state.registerSharedZone("pile", pile);
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());

        DrawCardsAction action = new DrawCardsAction(
                new ZoneRef("pile", Ownership.SHARED), new ZoneRef("hand", Ownership.PER_PLAYER), 2);
        RuleContext context = new RuleContext(state, Event.of("TURN_STARTED")).withTarget(ALICE);

        action.execute(context);

        assertEquals(0, pile.size());
        assertEquals(2, ((LinearZone) state.zoneOf(ALICE.id(), "hand")).size());
    }

    @Test
    void zeroCountDoesNothing() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c1"), Map.of()));
        state.registerSharedZone("pile", pile);
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());

        DrawCardsAction action = new DrawCardsAction(
                new ZoneRef("pile", Ownership.SHARED), new ZoneRef("hand", Ownership.PER_PLAYER), 0);
        RuleContext context = new RuleContext(state, Event.of("TURN_STARTED")).withTarget(ALICE);

        action.execute(context);

        assertEquals(1, pile.size());
    }

    @Test
    void rejectsNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> new DrawCardsAction(
                new ZoneRef("pile", Ownership.SHARED), new ZoneRef("hand", Ownership.PER_PLAYER), -1));
    }
}
