package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeclareWinnerActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void declaresTargetPlayerAsWinnerAndEndsGame() {
        GameState state = new GameState(List.of(ALICE, BOB));
        DeclareWinnerAction action = new DeclareWinnerAction();
        RuleContext context = new RuleContext(state, Event.of("CARD_PLAYED")).withTarget(ALICE);

        action.execute(context);

        assertTrue(state.isEnded());
        assertEquals(List.of(ALICE.id()), state.winners());
    }
}
