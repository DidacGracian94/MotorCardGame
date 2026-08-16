package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Comprueba si una zona referenciada está vacía. Para zonas {@code PER_PLAYER}, la zona
 * consultada es la del jugador cuyo turno está en curso — las condiciones se evalúan antes de
 * resolver el target de la regla, así que no hay otro jugador de contexto disponible todavía.
 */
public final class ZoneIsEmptyCondition implements Condition {

    private final ZoneRef zone;

    public ZoneIsEmptyCondition(ZoneRef zone) {
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolved = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        return resolved.isEmpty();
    }
}
