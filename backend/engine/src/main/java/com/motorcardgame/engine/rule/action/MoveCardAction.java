package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Mueve al tope de la zona {@code to} la carta identificada por {@code "cardId"} en el payload
 * del evento que disparó la regla, sacándola de la zona {@code from} — a diferencia de
 * {@link DrawCardsAction}, que mueve por posición (tope) y cantidad, esta acción mueve una carta
 * concreta elegida por quien originó el evento (típicamente un jugador jugando una carta de su
 * mano), no una cantidad fija de cartas del tope.
 */
public final class MoveCardAction implements Action {

    private final ZoneRef from;
    private final ZoneRef to;

    public MoveCardAction(ZoneRef from, ZoneRef to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    @Override
    public void execute(RuleContext context) {
        Object cardIdValue = context.event().payload("cardId");
        if (cardIdValue == null) {
            throw new IllegalStateException("MOVE_CARD requires a \"cardId\" key in the event payload");
        }
        CardId cardId = new CardId((String) cardIdValue);
        Player target = (Player) context.target().orElseThrow();
        LinearZone fromZone = (LinearZone) from.resolve(context.gameState(), target);
        LinearZone toZone = (LinearZone) to.resolve(context.gameState(), target);
        Card card = fromZone.removeById(cardId);
        toZone.pushTop(card);
    }
}
