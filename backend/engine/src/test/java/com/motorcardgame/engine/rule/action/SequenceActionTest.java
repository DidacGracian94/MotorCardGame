package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequenceActionTest {

    private static final RuleContext CONTEXT = new RuleContext(new GameState(List.of()), Event.of("TEST_EVENT"));

    @Test
    void executesActionsInOrder() {
        List<String> executed = new ArrayList<>();
        Action sequence = new SequenceAction(
                ctx -> executed.add("first"),
                ctx -> executed.add("second"));

        sequence.execute(CONTEXT);

        assertEquals(List.of("first", "second"), executed);
    }

    @Test
    void doesNothingWhenEmpty() {
        List<String> executed = new ArrayList<>();
        Action sequence = new SequenceAction();

        sequence.execute(CONTEXT);

        assertTrue(executed.isEmpty());
    }

    @Test
    void rejectsNullElement() {
        assertThrows(NullPointerException.class, () -> new SequenceAction((Action) null));
    }
}
