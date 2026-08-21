package com.motorcardgame.engine.rule.action;

import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.RuleContext;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.Objects;

/**
 * Suma, sobre el target (un jugador), el atributo entero {@code attribute} de todas las cartas
 * presentes en {@code zone} — p.ej. sumar los puntos de las cartas de una baza al jugador que la
 * ganó. Cartas sin ese atributo (o con un valor que no es entero) no puntúan, en vez de romper la
 * resolución: no todas las cartas de una zona tienen por qué puntuar en todos los juegos.
 */
public final class AddPointsAction implements Action {

    private final ZoneRef zone;
    private final String attribute;

    public AddPointsAction(ZoneRef zone, String attribute) {
        this.zone = Objects.requireNonNull(zone, "zone");
        this.attribute = Objects.requireNonNull(attribute, "attribute");
    }

    @Override
    public void execute(RuleContext context) {
        Player target = (Player) context.target().orElseThrow();
        LinearZone resolvedZone = (LinearZone) zone.resolve(context.gameState(), target);
        int sum = 0;
        for (Card card : resolvedZone.cardsView()) {
            if (card.attribute(attribute) instanceof Integer points) {
                sum += points;
            }
        }
        target.addScore(sum);
    }
}
