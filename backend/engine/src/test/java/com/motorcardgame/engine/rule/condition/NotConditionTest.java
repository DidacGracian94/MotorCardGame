package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotConditionTest {

    private static final RuleContext CONTEXT = new RuleContext(new GameState(List.of()), Event.of("TEST_EVENT"));

    @Test
    void negatesTrue() {
        Condition not = new NotCondition(ctx -> true);

        assertFalse(not.evaluate(CONTEXT));
    }

    @Test
    void negatesFalse() {
        Condition not = new NotCondition(ctx -> false);

        assertTrue(not.evaluate(CONTEXT));
    }

    @Test
    void rejectsNullWrappedCondition() {
        assertThrows(NullPointerException.class, () -> new NotCondition(null));
    }
}
