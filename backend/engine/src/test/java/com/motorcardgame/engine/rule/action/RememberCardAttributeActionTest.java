package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.condition.Position;
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

class RememberCardAttributeActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");

    @Test
    void remembersTopCardAttributeFromSharedZone() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone triunfo = new LinearZone();
        triunfo.pushTop(new Card(new CardId("c1"), Map.of("suit", "oros")));
        state.registerSharedZone("triunfo", triunfo);

        RememberCardAttributeAction action = new RememberCardAttributeAction(
                new ZoneRef("triunfo", Ownership.SHARED), Position.TOP, "suit", "trumpSuit");
        RuleContext context = new RuleContext(state, Event.of("GAME_STARTED")).withTarget(ALICE);

        action.execute(context);

        assertEquals("oros", state.variable("trumpSuit"));
    }

    @Test
    void remembersBottomCardAttribute() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("bottom"), Map.of("suit", "copas")));
        pile.pushTop(new Card(new CardId("top"), Map.of("suit", "espadas")));
        state.registerSharedZone("pile", pile);

        RememberCardAttributeAction action = new RememberCardAttributeAction(
                new ZoneRef("pile", Ownership.SHARED), Position.BOTTOM, "suit", "ledSuit");
        RuleContext context = new RuleContext(state, Event.of("GAME_STARTED")).withTarget(ALICE);

        action.execute(context);

        assertEquals("copas", state.variable("ledSuit"));
    }

    @Test
    void remembersFromPerPlayerZone() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone hand = new LinearZone();
        hand.pushTop(new Card(new CardId("c1"), Map.of("rank", 7)));
        state.registerPlayerZone(ALICE.id(), "hand", hand);

        RememberCardAttributeAction action = new RememberCardAttributeAction(
                new ZoneRef("hand", Ownership.PER_PLAYER), Position.TOP, "rank", "lastRank");
        RuleContext context = new RuleContext(state, Event.of("GAME_STARTED")).withTarget(ALICE);

        action.execute(context);

        assertEquals(7, state.variable("lastRank"));
    }
}
