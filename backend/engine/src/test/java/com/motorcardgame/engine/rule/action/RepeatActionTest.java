package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepeatActionTest {

    private static final RuleContext CONTEXT = new RuleContext(new GameState(List.of()), Event.of("TEST_EVENT"));

    @Test
    void executesActionGivenNumberOfTimes() {
        int[] count = {0};
        Action repeat = new RepeatAction(ctx -> count[0]++, 3);

        repeat.execute(CONTEXT);

        assertEquals(3, count[0]);
    }

    @Test
    void doesNothingWhenTimesIsZero() {
        int[] count = {0};
        Action repeat = new RepeatAction(ctx -> count[0]++, 0);

        repeat.execute(CONTEXT);

        assertEquals(0, count[0]);
    }

    @Test
    void rejectsNegativeTimes() {
        assertThrows(IllegalArgumentException.class, () -> new RepeatAction(ctx -> {
        }, -1));
    }

    @Test
    void rejectsNullAction() {
        assertThrows(NullPointerException.class, () -> new RepeatAction(null, 1));
    }
}
