package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentPlayerTargetTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void resolvesToCurrentPlayerOnly() {
        GameState state = new GameState(List.of(ALICE, BOB));
        state.advanceTurn();

        List<Object> resolved = new CurrentPlayerTarget().resolve(new RuleContext(state, Event.of("TICK")));

        assertEquals(List.of(BOB), resolved);
    }
}
