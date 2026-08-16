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

class CardAttributeEqualsConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueWhenTopCardAttributeMatches() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c1"), Map.of("color", "RED")));
        state.registerSharedZone("pile", pile);

        CardAttributeEqualsCondition condition = new CardAttributeEqualsCondition(
                new ZoneRef("pile", Ownership.SHARED), Position.TOP, "color", "RED");

        assertTrue(condition.evaluate(new RuleContext(state, Event.of("TURN_STARTED"))));
    }

    @Test
    void falseWhenAttributeDiffers() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c1"), Map.of("color", "BLUE")));
        state.registerSharedZone("pile", pile);

        CardAttributeEqualsCondition condition = new CardAttributeEqualsCondition(
                new ZoneRef("pile", Ownership.SHARED), Position.TOP, "color", "RED");

        assertFalse(condition.evaluate(new RuleContext(state, Event.of("TURN_STARTED"))));
    }

    @Test
    void looksAtBottomCardWhenPositionIsBottom() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("bottom"), Map.of("color", "GREEN")));
        pile.pushTop(new Card(new CardId("top"), Map.of("color", "RED")));
        state.registerSharedZone("pile", pile);

        CardAttributeEqualsCondition condition = new CardAttributeEqualsCondition(
                new ZoneRef("pile", Ownership.SHARED), Position.BOTTOM, "color", "GREEN");

        assertTrue(condition.evaluate(new RuleContext(state, Event.of("TURN_STARTED"))));
    }
}
