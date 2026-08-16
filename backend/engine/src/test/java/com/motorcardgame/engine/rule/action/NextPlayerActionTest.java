package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NextPlayerActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void advancesToNextPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));
        NextPlayerAction action = new NextPlayerAction();

        action.execute(new RuleContext(state, Event.of("TURN_ENDED")));

        assertEquals(BOB, state.currentPlayer());
    }
}
