package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Comprueba si una zona referenciada tiene exactamente {@code count} cartas — generaliza
 * {@link ZoneIsEmptyCondition} (que es el caso {@code count == 0}) para detectar, p.ej., que una
 * zona de mesa ya tiene una carta por jugador y toca resolver la baza.
 */
public final class ZoneCardCountEqualsCondition implements Condition {

    private final ZoneRef zone;
    private final int count;

    public ZoneCardCountEqualsCondition(ZoneRef zone, int count) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.count = count;
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolved = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        return resolved.size() == count;
    }
}
