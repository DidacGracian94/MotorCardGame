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

class AnyCardAttributeMatchesZoneConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueWhenAnyCardInZoneMatchesReferenceTop() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of("suit", "copas")));
        hand.pushTop(new Card(new CardId("c2"), Map.of("suit", "espadas")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("led"), Map.of("suit", "espadas")));
        state.registerSharedZone("mesa", mesa);

        AnyCardAttributeMatchesZoneCondition condition = new AnyCardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "suit",
                new ZoneRef("mesa", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD"));

        assertTrue(condition.evaluate(context));
    }

    @Test
    void falseWhenNoCardInZoneMatchesReference() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of("suit", "oros")));
        hand.pushTop(new Card(new CardId("c2"), Map.of("suit", "bastos")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("led"), Map.of("suit", "espadas")));
        state.registerSharedZone("mesa", mesa);

        AnyCardAttributeMatchesZoneCondition condition = new AnyCardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "suit",
                new ZoneRef("mesa", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD"));

        assertFalse(condition.evaluate(context));
    }

    @Test
    void looksAtBottomOfReferenceZoneWhenReferencePositionIsBottom() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of("suit", "espadas")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("bottom"), Map.of("suit", "espadas")));
        mesa.pushTop(new Card(new CardId("top"), Map.of("suit", "oros")));
        state.registerSharedZone("mesa", mesa);

        AnyCardAttributeMatchesZoneCondition condition = new AnyCardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "suit",
                new ZoneRef("mesa", Ownership.SHARED), Position.BOTTOM);
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD"));

        assertTrue(condition.evaluate(context));
    }

    @Test
    void falseWhenReferenceZoneIsEmpty() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of("suit", "espadas")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        state.registerSharedZone("mesa", new LinearZone());

        AnyCardAttributeMatchesZoneCondition condition = new AnyCardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "suit",
                new ZoneRef("mesa", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD"));

        assertFalse(condition.evaluate(context));
    }

    @Test
    void falseWhenZoneItselfIsEmpty() {
        GameState state = new GameState(List.of(ALICE));
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        LinearZone mesa = new LinearZone();
        mesa.pushTop(new Card(new CardId("led"), Map.of("suit", "espadas")));
        state.registerSharedZone("mesa", mesa);

        AnyCardAttributeMatchesZoneCondition condition = new AnyCardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "suit",
                new ZoneRef("mesa", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, Event.of("PLAY_CARD"));

        assertFalse(condition.evaluate(context));
    }
}
