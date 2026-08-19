package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Comprueba si la carta identificada por {@code "cardId"} en el payload del evento está
 * actualmente en {@code zone} — a diferencia de {@link EventCardAttributeEqualsCondition} o
 * {@link CardAttributeMatchesZoneCondition}, que asumen que la carta referida sigue en la zona que
 * se les indica y lanzan si no la encuentran, esta condición existe justo para el caso contrario:
 * saber, sin asumir nada, si esa carta ya llegó a una zona concreta. Es lo que permite componer
 * "una regla ya movió esta carta a {@code zone} si la jugada era legal" en vez de repetir la
 * comprobación de legalidad original — cuya zona de origen (p.ej. la mano) ya no contiene la
 * carta una vez movida, así que reevaluarla ahí lanzaría en vez de dar {@code false}.
 */
public final class EventCardInZoneCondition implements Condition {

    private final ZoneRef zone;

    public EventCardInZoneCondition(ZoneRef zone) {
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Object cardIdValue = context.event().payload("cardId");
        if (!(cardIdValue instanceof String cardIdString)) {
            return false;
        }
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolvedZone = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        CardId cardId = new CardId(cardIdString);
        return resolvedZone.cardsView().stream().anyMatch(card -> card.id().equals(cardId));
    }
}
