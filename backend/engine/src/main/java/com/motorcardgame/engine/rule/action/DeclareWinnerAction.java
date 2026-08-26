package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Player;

/**
 * Declara ganador al jugador resuelto por el target de la regla y marca la partida como
 * terminada — genérico para cualquier condición de victoria expresable con las capacidades
 * existentes del motor (p.ej. {@code CURRENT_PLAYER} al vaciar la mano en UNO), no específico de
 * ningún juego.
 */
public final class DeclareWinnerAction implements Action {

    @Override
    public void execute(RuleContext context) {
        Player winner = (Player) context.target().orElseThrow();
        context.gameState().declareWinner(winner.id());
    }
}
