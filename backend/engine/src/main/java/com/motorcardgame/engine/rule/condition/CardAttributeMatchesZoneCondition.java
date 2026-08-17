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
 * (localizada en {@code cardZone}) contra el mismo atributo del tope o fondo de otra zona
 * ({@code zone}) — a diferencia de {@link CardAttributeEqualsCondition}, que compara contra un
 * valor literal fijo en la config, esta condición compara dos cartas dinámicas entre sí (p.ej.
 * "la carta que el jugador quiere jugar" contra "el tope del descarte").
 */
public final class CardAttributeMatchesZoneCondition implements Condition {

    private final ZoneRef cardZone;
    private final String attribute;
    private final ZoneRef zone;
    private final Position position;

    public CardAttributeMatchesZoneCondition(ZoneRef cardZone, String attribute, ZoneRef zone, Position position) {
        this.cardZone = Objects.requireNonNull(cardZone, "cardZone");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.zone = Objects.requireNonNull(zone, "zone");
        this.position = Objects.requireNonNull(position, "position");
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Object cardIdValue = context.event().payload("cardId");
        if (cardIdValue == null) {
            throw new IllegalStateException(
                    "CARD_ATTRIBUTE_MATCHES_ZONE requires a \"cardId\" key in the event payload");
        }
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolvedCardZone = (LinearZone) cardZone.resolve(context.gameState(), currentPlayer);
        Card referencedCard = resolvedCardZone.findById(new CardId((String) cardIdValue));

        LinearZone resolvedZone = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        Card zoneCard = position == Position.TOP ? resolvedZone.peekTop() : resolvedZone.peekBottom();

        return Objects.equals(referencedCard.attribute(attribute), zoneCard.attribute(attribute));
    }
}
