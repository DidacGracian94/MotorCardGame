package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Lee el atributo {@code attribute} de la carta en {@code position} de {@code zone} y lo guarda en
 * {@link com.motorcardgame.engine.state.GameState#setVariable(String, Object)} bajo el nombre
 * {@code as} — a diferencia de comparar contra la carta de una zona en el momento (p.ej.
 * {@code CARD_ATTRIBUTE_MATCHES_ZONE}), este valor sigue disponible aunque esa carta después se
 * mueva a otra zona (p.ej. recordar el palo de triunfo antes de que la carta reveladora acabe en
 * la mano de un jugador).
 */
public final class RememberCardAttributeAction implements Action {

    private final ZoneRef zone;
    private final Position position;
    private final String attribute;
    private final String as;

    public RememberCardAttributeAction(ZoneRef zone, Position position, String attribute, String as) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.position = Objects.requireNonNull(position, "position");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.as = Objects.requireNonNull(as, "as");
    }

    @Override
    public void execute(RuleContext context) {
        Player target = (Player) context.target().orElseThrow();
        LinearZone resolvedZone = (LinearZone) zone.resolve(context.gameState(), target);
        Card card = position == Position.TOP ? resolvedZone.peekTop() : resolvedZone.peekBottom();
        context.gameState().setVariable(as, card.attribute(attribute));
    }
}
