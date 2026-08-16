package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrConditionTest {

    private static final RuleContext CONTEXT = new RuleContext(new GameState(List.of()), Event.of("TEST_EVENT"));

    @Test
    void isTrueWhenAnyConditionIsTrue() {
        Condition or = new OrCondition(ctx -> false, ctx -> true);

        assertTrue(or.evaluate(CONTEXT));
    }

    @Test
    void isFalseWhenAllConditionsAreFalse() {
        Condition or = new OrCondition(ctx -> false, ctx -> false);

        assertFalse(or.evaluate(CONTEXT));
    }

    @Test
    void isFalseWhenEmpty() {
        Condition or = new OrCondition();

        assertFalse(or.evaluate(CONTEXT));
    }

    @Test
    void shortCircuitsOnFirstTrue() {
        boolean[] evaluatedSecond = {false};
        Condition or = new OrCondition(
                ctx -> true,
                ctx -> {
                    evaluatedSecond[0] = true;
                    return false;
                });

        or.evaluate(CONTEXT);

        assertFalse(evaluatedSecond[0]);
    }
}
