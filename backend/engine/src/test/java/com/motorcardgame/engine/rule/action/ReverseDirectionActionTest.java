package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseDirectionActionTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");
    private static final Player CAROL = new Player(new PlayerId("carol"), "Carol");

    @Test
    void makesAdvanceTurnGoBackwards() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));
        new ReverseDirectionAction().execute(new RuleContext(state, Event.of("CARD_PLAYED")));

        state.advanceTurn();

        assertEquals(CAROL, state.currentPlayer());
    }

    @Test
    void appliedTwiceRestoresForwardDirection() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));
        RuleContext context = new RuleContext(state, Event.of("CARD_PLAYED"));

        new ReverseDirectionAction().execute(context);
        new ReverseDirectionAction().execute(context);
        state.advanceTurn();

        assertEquals(BOB, state.currentPlayer());
    }
}
