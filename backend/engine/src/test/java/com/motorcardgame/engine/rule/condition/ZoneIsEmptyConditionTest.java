package com.motorcardgame.engine.rule.condition;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneIsEmptyConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueForEmptySharedZone() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("pile", new LinearZone());
        ZoneIsEmptyCondition condition = new ZoneIsEmptyCondition(new ZoneRef("pile", Ownership.SHARED));

        assertTrue(condition.evaluate(new RuleContext(state, Event.of("TURN_STARTED"))));
    }

    @Test
    void falseForNonEmptyPerPlayerZone() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of()));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        ZoneIsEmptyCondition condition = new ZoneIsEmptyCondition(new ZoneRef("hand", Ownership.PER_PLAYER));

        assertFalse(condition.evaluate(new RuleContext(state, Event.of("TURN_STARTED"))));
    }
}
