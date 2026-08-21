package com.motorcardgame.engine.rule.target;

import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.Target;

import java.util.List;

/**
 * Resuelve al jugador al que le tocaría a continuación (respetando el sentido de turno actual),
 * sin avanzar el turno — para poder aplicarle una acción (p.ej. robar cartas) a esa persona en
 * concreto antes de que sea su turno, como en una carta de "chupa 2".
 */
public final class NextPlayerTarget implements Target {

    @Override
    public List<Object> resolve(RuleContext context) {
        return List.of(context.gameState().nextPlayer());
    }
}
