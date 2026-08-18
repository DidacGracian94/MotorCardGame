package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Compara un atributo de la carta identificada por {@code "cardId"} en el payload del evento
 * (localizada en {@code cardZone}) contra un valor literal fijo en la config — a diferencia de
 * {@link CardAttributeMatchesZoneCondition}, que compara esa misma carta contra el tope/fondo de
 * otra zona, esta condición compara contra un valor fijo. Es la pieza que faltaba para poder
 * identificar "la carta que estoy jugando es de este tipo concreto" (p.ej. una carta especial
 * como "chupa 2" o "cambio de sentido"), algo que ni {@link CardAttributeEqualsCondition} (no
 * conoce el cardId del evento, solo mira tope/fondo de una zona) ni
 * {@link CardAttributeMatchesZoneCondition} (compara contra otra zona, no contra un literal)
 * cubrían.
 */
public final class EventCardAttributeEqualsCondition implements Condition {

    private final ZoneRef cardZone;
    private final String attribute;
    private final Object expected;

    public EventCardAttributeEqualsCondition(ZoneRef cardZone, String attribute, Object expected) {
        this.cardZone = Objects.requireNonNull(cardZone, "cardZone");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.expected = expected;
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Object cardIdValue = context.event().payload("cardId");
        if (cardIdValue == null) {
            throw new IllegalStateException(
                    "EVENT_CARD_ATTRIBUTE_EQUALS requires a \"cardId\" key in the event payload");
        }
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolvedCardZone = (LinearZone) cardZone.resolve(context.gameState(), currentPlayer);
        Card referencedCard = resolvedCardZone.findById(new CardId((String) cardIdValue));
        return Objects.equals(expected, referencedCard.attribute(attribute));
    }
}
