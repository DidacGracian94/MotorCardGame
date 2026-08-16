package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;

/**
 * Avanza el turno al siguiente jugador.
 */
public final class NextPlayerAction implements Action {

    @Override
    public void execute(RuleContext context) {
        context.gameState().advanceTurn();
    }
}
