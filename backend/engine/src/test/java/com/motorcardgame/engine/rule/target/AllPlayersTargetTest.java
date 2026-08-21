package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllPlayersTargetTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void resolvesToEveryPlayerInOrder() {
        GameState state = new GameState(List.of(ALICE, BOB));

        List<Object> resolved = new AllPlayersTarget().resolve(new RuleContext(state, Event.of("TICK")));

        assertEquals(List.of(ALICE, BOB), resolved);
    }
}
