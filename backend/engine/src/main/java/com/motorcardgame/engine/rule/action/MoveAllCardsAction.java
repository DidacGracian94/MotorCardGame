package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Vacía la zona {@code from} entera, moviendo cada carta a {@code to} — a diferencia de
 * {@link MoveCardAction} (una carta concreta) y {@link DrawCardsAction} (una cantidad fija), esta
 * acción no depende de cuántas cartas haya ni de conocer sus ids; p.ej. recoger todas las cartas
 * de una baza para el jugador que la ganó, sin que el número de jugadores condicione la regla.
 */
public final class MoveAllCardsAction implements Action {

    private final ZoneRef from;
    private final ZoneRef to;

    public MoveAllCardsAction(ZoneRef from, ZoneRef to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    @Override
    public void execute(RuleContext context) {
        Player target = (Player) context.target().orElseThrow();
        LinearZone fromZone = (LinearZone) from.resolve(context.gameState(), target);
        LinearZone toZone = (LinearZone) to.resolve(context.gameState(), target);
        while (!fromZone.isEmpty()) {
            toZone.pushTop(fromZone.popTop());
        }
    }
}
