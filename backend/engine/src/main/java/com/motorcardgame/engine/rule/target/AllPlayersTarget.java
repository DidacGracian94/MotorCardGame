package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.Target;

import java.util.List;

/**
 * Resuelve a todos los jugadores de la partida.
 */
public final class AllPlayersTarget implements Target {

    @Override
    public List<Object> resolve(RuleContext context) {
        return context.gameState().players().stream().map(player -> (Object) player).toList();
    }
}
