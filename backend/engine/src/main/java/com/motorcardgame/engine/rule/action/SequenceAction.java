package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;

import java.util.List;

/**
 * Ejecuta varias acciones en orden. Sin acciones, no hace nada.
 */
public final class SequenceAction implements Action {

    private final List<Action> actions;

    public SequenceAction(Action... actions) {
        this.actions = List.of(actions);
    }

    @Override
    public void execute(RuleContext context) {
        for (Action action : actions) {
            action.execute(context);
        }
    }
}
