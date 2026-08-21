package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.Target;

import java.util.List;

/**
 * Resuelve al jugador cuyo turno está en curso.
 */
public final class CurrentPlayerTarget implements Target {

    @Override
    public List<Object> resolve(RuleContext context) {
        return List.of(context.gameState().currentPlayer());
    }
}
