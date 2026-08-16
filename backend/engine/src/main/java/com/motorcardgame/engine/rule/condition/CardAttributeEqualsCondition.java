package com.motorcardgame.engine.rule.condition;

import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Compara un atributo de la carta en el tope o el fondo de una zona referenciada contra un valor
 * esperado. Los atributos de {@link Card} son opacos al motor (ver {@link Card}), así que esta
 * condición sirve para cualquier atributo que defina la configuración del juego (color, número,
 * palo, ...), no solo para UNO.
 */
public final class CardAttributeEqualsCondition implements Condition {

    private final ZoneRef zone;
    private final Position position;
    private final String attribute;
    private final Object expected;

    public CardAttributeEqualsCondition(ZoneRef zone, Position position, String attribute, Object expected) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.position = Objects.requireNonNull(position, "position");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.expected = expected;
    }

    @Override
    public boolean evaluate(RuleContext context) {
        Player currentPlayer = context.gameState().currentPlayer();
        LinearZone resolved = (LinearZone) zone.resolve(context.gameState(), currentPlayer);
        Card card = position == Position.TOP ? resolved.peekTop() : resolved.peekBottom();
        return Objects.equals(expected, card.attribute(attribute));
    }
}
