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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardAttributeMatchesZoneConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueWhenReferencedCardAttributeMatchesZoneTopCard() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("played"), Map.of("color", "RED")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone discard = new LinearZone();
        discard.pushTop(new Card(new CardId("top"), Map.of("color", "RED")));
        state.registerSharedZone("discard", discard);

        CardAttributeMatchesZoneCondition condition = new CardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "color",
                new ZoneRef("discard", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "played")));

        assertTrue(condition.evaluate(context));
    }

    @Test
    void falseWhenAttributeDiffers() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("played"), Map.of("color", "RED")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone discard = new LinearZone();
        discard.pushTop(new Card(new CardId("top"), Map.of("color", "BLUE")));
        state.registerSharedZone("discard", discard);

        CardAttributeMatchesZoneCondition condition = new CardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "color",
                new ZoneRef("discard", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "played")));

        assertFalse(condition.evaluate(context));
    }

    @Test
    void looksAtBottomOfZoneWhenPositionIsBottom() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("played"), Map.of("color", "GREEN")));
        state.registerPlayerZone(ALICE.id(), "hand", hand);
        LinearZone discard = new LinearZone();
        discard.pushTop(new Card(new CardId("bottom"), Map.of("color", "GREEN")));
        discard.pushTop(new Card(new CardId("top"), Map.of("color", "RED")));
        state.registerSharedZone("discard", discard);

        CardAttributeMatchesZoneCondition condition = new CardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "color",
                new ZoneRef("discard", Ownership.SHARED), Position.BOTTOM);
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "played")));

        assertTrue(condition.evaluate(context));
    }

    @Test
    void throwsWhenPayloadHasNoCardId() {
        GameState state = new GameState(List.of(ALICE));
        state.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        LinearZone discard = new LinearZone();
        discard.pushTop(new Card(new CardId("top"), Map.of("color", "RED")));
        state.registerSharedZone("discard", discard);

        CardAttributeMatchesZoneCondition condition = new CardAttributeMatchesZoneCondition(
                new ZoneRef("hand", Ownership.PER_PLAYER), "color",
                new ZoneRef("discard", Ownership.SHARED), Position.TOP);
        RuleContext context = new RuleContext(state, Event.of("CARD_PLAYED"));

        assertThrows(IllegalStateException.class, () -> condition.evaluate(context));
    }
}
