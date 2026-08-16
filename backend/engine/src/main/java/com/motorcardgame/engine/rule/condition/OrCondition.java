package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;

import java.util.List;

/**
 * Combina varias condiciones con OR, evaluando con corto-circuito (se detiene en la primera
 * {@code true}). Sin condiciones, evalúa a {@code false} (elemento neutro de OR).
 */
public final class OrCondition implements Condition {

    private final List<Condition> conditions;

    public OrCondition(Condition... conditions) {
        this.conditions = List.of(conditions);
    }

    @Override
    public boolean evaluate(RuleContext context) {
        for (Condition condition : conditions) {
            if (condition.evaluate(context)) {
                return true;
            }
        }
        return false;
    }
}
