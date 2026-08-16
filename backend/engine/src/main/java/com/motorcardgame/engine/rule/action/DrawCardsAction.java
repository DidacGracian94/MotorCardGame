package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Mueve {@code count} cartas del tope de la zona {@code from} al tope de la zona {@code to}. El
 * target de la regla (normalmente un jugador) es el contexto para resolver zonas
 * {@code PER_PLAYER} — a diferencia de una {@link com.motorcardgame.engine.rule.Condition}, una
 * acción ya se ejecuta con el target resuelto.
 */
public final class DrawCardsAction implements Action {

    private final ZoneRef from;
    private final ZoneRef to;
    private final int count;

    public DrawCardsAction(ZoneRef from, ZoneRef to, int count) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative: " + count);
        }
        this.count = count;
    }

    @Override
    public void execute(RuleContext context) {
        Player target = (Player) context.target().orElseThrow();
        LinearZone fromZone = (LinearZone) from.resolve(context.gameState(), target);
        LinearZone toZone = (LinearZone) to.resolve(context.gameState(), target);
        for (int i = 0; i < count; i++) {
            Card card = fromZone.popTop();
            toZone.pushTop(card);
        }
    }
}
