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

class EventCardInZoneConditionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void trueWhenReferencedCardIsInTheZone() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone discard = new LinearZone();
        discard.pushTop(new Card(new CardId("reverso-rojo#1"), Map.of()));
        state.registerSharedZone("discard", discard);

        EventCardInZoneCondition condition = new EventCardInZoneCondition(
                new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "reverso-rojo#1")));

        assertTrue(condition.evaluate(context));
    }

    @Test
    void falseWhenTheCardIsNotInTheZone() {
        // Caso real: la carta todavía está en la mano porque la jugada resultó ilegal y ninguna
        // regla la movió al descarte — no debe lanzar, solo evaluar a false.
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("discard", new LinearZone());

        EventCardInZoneCondition condition = new EventCardInZoneCondition(
                new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", "reverso-rojo#1")));

        assertFalse(condition.evaluate(context));
    }

    @Test
    void falseWhenPayloadHasNoCardId() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("discard", new LinearZone());

        EventCardInZoneCondition condition = new EventCardInZoneCondition(
                new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, Event.of("CARD_PLAYED"));

        assertFalse(condition.evaluate(context));
    }

    @Test
    void falseWhenCardIdPayloadIsNotAString() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("discard", new LinearZone());

        EventCardInZoneCondition condition = new EventCardInZoneCondition(
                new ZoneRef("discard", Ownership.SHARED));
        RuleContext context = new RuleContext(state, new Event("CARD_PLAYED", Map.of("cardId", 42)));

        assertFalse(condition.evaluate(context));
    }
}
