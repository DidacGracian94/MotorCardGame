package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;

import java.util.Objects;

/**
 * Ejecuta una acción envuelta un número fijo de veces. {@code times} igual a cero es válido
 * (no ejecuta nada); negativo se rechaza en construcción por ser un error del caller.
 */
public final class RepeatAction implements Action {

    private final Action action;
    private final int times;

    public RepeatAction(Action action, int times) {
        this.action = Objects.requireNonNull(action, "action");
        if (times < 0) {
            throw new IllegalArgumentException("times must not be negative: " + times);
        }
        this.times = times;
    }

    @Override
    public void execute(RuleContext context) {
        for (int i = 0; i < times; i++) {
            action.execute(context);
        }
    }
}
