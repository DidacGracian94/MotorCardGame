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
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveAllCardsActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void movesEveryCardFromSourceToDestination() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("c1"), Map.of()));
        mesa.pushTop(new Card(new CardId("c2"), Map.of()));
        state.registerSharedZone("mesa", mesa);
        state.registerPlayerZone(ALICE.id(), "bazas", new LinearZone());

        MoveAllCardsAction action = new MoveAllCardsAction(
                new ZoneRef("mesa", Ownership.SHARED), new ZoneRef("bazas", Ownership.PER_PLAYER));
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD")).withTarget(ALICE);

        action.execute(context);

        assertTrue(mesa.isEmpty());
        assertEquals(2, ((LinearZone) state.zoneOf(ALICE.id(), "bazas")).size());
    }

    @Test
    void doesNothingWhenSourceIsAlreadyEmpty() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("mesa", new LinearZone());
        state.registerPlayerZone(ALICE.id(), "bazas", new LinearZone());

        MoveAllCardsAction action = new MoveAllCardsAction(
                new ZoneRef("mesa", Ownership.SHARED), new ZoneRef("bazas", Ownership.PER_PLAYER));
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD")).withTarget(ALICE);

        action.execute(context);

        assertEquals(0, ((LinearZone) state.zoneOf(ALICE.id(), "bazas")).size());
    }
}
