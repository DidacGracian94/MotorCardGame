package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;

import java.util.Objects;

/**
 * Niega el resultado de una condición envuelta.
 */
public final class NotCondition implements Condition {

    private final Condition wrapped;

    public NotCondition(Condition wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
    }

    @Override
    public boolean evaluate(RuleContext context) {
        return !wrapped.evaluate(context);
    }
}
