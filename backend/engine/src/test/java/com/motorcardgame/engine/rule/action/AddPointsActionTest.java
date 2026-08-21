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

class AddPointsActionTest {

    // Player.score is mutable, so each test builds its own instance instead of sharing a static
    // constant -- otherwise score accumulated in one test would leak into the next.

    @Test
    void sumsIntegerAttributeAcrossZoneOntoTargetScore() {
        Player alice = new Player(new PlayerId("alice"), "Alice");
        GameState state = new GameState(List.of(alice));
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("c1"), Map.of("points", 11)));
        mesa.pushTop(new Card(new CardId("c2"), Map.of("points", 4)));
        state.registerSharedZone("mesa", mesa);

        AddPointsAction action = new AddPointsAction(new ZoneRef("mesa", Ownership.SHARED), "points");
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD")).withTarget(alice);

        action.execute(context);

        assertEquals(15, alice.score());
    }

    @Test
    void ignoresCardsWithoutTheAttribute() {
        Player alice = new Player(new PlayerId("alice"), "Alice");
        GameState state = new GameState(List.of(alice));
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("c1"), Map.of("points", 10)));
        mesa.pushTop(new Card(new CardId("c2"), Map.of()));
        state.registerSharedZone("mesa", mesa);

        AddPointsAction action = new AddPointsAction(new ZoneRef("mesa", Ownership.SHARED), "points");
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD")).withTarget(alice);

        action.execute(context);

        assertEquals(10, alice.score());
    }
}
