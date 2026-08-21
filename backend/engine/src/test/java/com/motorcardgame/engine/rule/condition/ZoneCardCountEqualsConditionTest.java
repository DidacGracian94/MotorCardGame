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

class ZoneCardCountEqualsConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueWhenZoneHasExactCount() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("c1"), Map.of()));
        mesa.pushTop(new Card(new CardId("c2"), Map.of()));
        state.registerSharedZone("mesa", mesa);
        ZoneCardCountEqualsCondition condition =
                new ZoneCardCountEqualsCondition(new ZoneRef("mesa", Ownership.SHARED), 2);

        assertTrue(condition.evaluate(new RuleContext(state, Event.of("PLAY_CARD"))));
    }

    @Test
    void falseWhenZoneHasDifferentCount() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("c1"), Map.of()));
        state.registerSharedZone("mesa", mesa);
        ZoneCardCountEqualsCondition condition =
                new ZoneCardCountEqualsCondition(new ZoneRef("mesa", Ownership.SHARED), 2);

        assertFalse(condition.evaluate(new RuleContext(state, Event.of("PLAY_CARD"))));
    }

    @Test
    void trueForEmptyZoneWithCountZero() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("mesa", new LinearZone());
        ZoneCardCountEqualsCondition condition =
                new ZoneCardCountEqualsCondition(new ZoneRef("mesa", Ownership.SHARED), 0);

        assertTrue(condition.evaluate(new RuleContext(state, Event.of("PLAY_CARD"))));
    }
}
