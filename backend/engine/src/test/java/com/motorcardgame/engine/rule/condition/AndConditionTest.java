package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndConditionTest {

    private static final RuleContext CONTEXT = new RuleContext(new GameState(List.of()), Event.of("TEST_EVENT"));

    @Test
    void isTrueWhenAllConditionsAreTrue() {
        Condition and = new AndCondition(ctx -> true, ctx -> true);

        assertTrue(and.evaluate(CONTEXT));
    }

    @Test
    void isFalseWhenAnyConditionIsFalse() {
        Condition and = new AndCondition(ctx -> true, ctx -> false);

        assertFalse(and.evaluate(CONTEXT));
    }

    @Test
    void isTrueWhenEmpty() {
        Condition and = new AndCondition();

        assertTrue(and.evaluate(CONTEXT));
    }

    @Test
    void shortCircuitsOnFirstFalse() {
        boolean[] evaluatedSecond = {false};
        Condition and = new AndCondition(
                ctx -> false,
                ctx -> {
                    evaluatedSecond[0] = true;
                    return true;
                });

        and.evaluate(CONTEXT);

        assertFalse(evaluatedSecond[0]);
    }
}
