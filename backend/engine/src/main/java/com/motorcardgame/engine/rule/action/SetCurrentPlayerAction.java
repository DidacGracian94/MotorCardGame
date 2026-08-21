package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Player;

/**
 * Cede el turno al target resuelto por la regla (típicamente un target dinámico como
 * {@code WINNING_CARD_OWNER}) — a diferencia de {@link NextPlayerAction}, que solo avanza
 * secuencialmente, esta acción salta directamente a un jugador concreto sea cual sea su posición
 * en el orden de asiento.
 */
public final class SetCurrentPlayerAction implements Action {

    @Override
    public void execute(RuleContext context) {
        Player target = (Player) context.target().orElseThrow();
        context.gameState().setCurrentPlayer(target.id());
    }
}
