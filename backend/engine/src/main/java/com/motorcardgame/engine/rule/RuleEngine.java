package com.motorcardgame.engine.rule;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.state.GameState;

import java.util.List;

/**
 * Interpreta un conjunto fijo de {@link Rule} contra un {@link GameState}: al recibir un
 * {@link Event}, evalúa las reglas cuyo tipo de evento coincide, y para las que su condición se
 * cumple, ejecuta la acción una vez por cada elemento resuelto por el target. Devuelve si al
 * menos una regla coincidió en tipo de evento y tuvo condición verdadera — la señal genérica que
 * usa {@code app} para distinguir un evento que causó efecto de uno que no (p.ej. una jugada de
 * un jugador que ninguna regla permite).
 */
public final class RuleEngine {

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public boolean handle(Event event, GameState gameState) {
        RuleContext context = new RuleContext(gameState, event);
        boolean matched = false;
        for (Rule rule : rules) {
            if (!rule.eventType().equals(event.type())) {
                continue;
            }
            if (!rule.condition().evaluate(context)) {
                continue;
            }
            matched = true;
            for (Object resolved : rule.target().resolve(context)) {
                rule.action().execute(context.withTarget(resolved));
            }
        }
        return matched;
    }
}
