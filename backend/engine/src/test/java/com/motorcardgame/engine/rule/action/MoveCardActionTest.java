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
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveCardActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void movesNamedCardFromHandToDiscard() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of()));
        hand.pushTop(new Card(new CardId("c2"), Map.of()));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        state.registerSharedZone("discard", new LinearZone());

        MoveCardAction action = new MoveCardAction(
                new ZoneRef("hand", Ownership.PER_PLAYER), new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "c1")))
                .withTarget(ALICE);

        action.execute(context);

        assertEquals(1, hand.size());
        assertEquals(new CardId("c2"), hand.peekTop().id());
        assertEquals(new CardId("c1"), ((LinearZone) state.sharedZone("discard")).peekTop().id());
    }

    @Test
    void throwsWhenPayloadHasNoCardId() {
        GameState state = new GameState(List.of(ALICE));
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        state.registerSharedZone("discard", new LinearZone());

        MoveCardAction action = new MoveCardAction(
                new ZoneRef("hand", Ownership.PER_PLAYER), new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, Event.of("CARD_PLAYED")).withTarget(ALICE);

        assertThrows(IllegalStateException.class, () -> action.execute(context));
    }

    @Test
    void throwsWhenCardIdNotInFromZone() {
        GameState state = new GameState(List.of(ALICE));
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        state.registerSharedZone("discard", new LinearZone());

        MoveCardAction action = new MoveCardAction(
                new ZoneRef("hand", Ownership.PER_PLAYER), new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "missing")))
                .withTarget(ALICE);

        assertThrows(NoSuchElementException.class, () -> action.execute(context));
    }
}
