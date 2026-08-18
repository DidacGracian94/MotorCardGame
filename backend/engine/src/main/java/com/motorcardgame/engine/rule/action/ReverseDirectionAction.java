package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;

/**
 * Invierte el sentido en el que avanza el turno (p.ej. al jugar una carta de "cambio de
 * sentido"). En una partida de 2 jugadores tiene el mismo efecto visible que saltar el turno del
 * otro jugador; con 3 o más, cambia quién juega a continuación.
 */
public final class ReverseDirectionAction implements Action {

    @Override
    public void execute(RuleContext context) {
        context.gameState().reverseDirection();
    }
}
