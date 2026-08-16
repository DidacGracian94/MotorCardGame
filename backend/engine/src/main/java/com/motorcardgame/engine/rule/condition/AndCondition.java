package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;

import java.util.List;

/**
 * Combina varias condiciones con AND, evaluando con corto-circuito (se detiene en la primera
 * {@code false}). Sin condiciones, evalúa a {@code true} (elemento neutro de AND).
 */
public final class AndCondition implements Condition {

    private final List<Condition> conditions;

    public AndCondition(Condition... conditions) {
        this.conditions = List.of(conditions);
    }

    @Override
    public boolean evaluate(RuleContext context) {
        for (Condition condition : conditions) {
            if (!condition.evaluate(context)) {
                return false;
            }
        }
        return true;
    }
}
