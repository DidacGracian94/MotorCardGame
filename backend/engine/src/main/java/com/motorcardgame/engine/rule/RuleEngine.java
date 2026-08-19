package com.motorcardgame.engine.rule;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.GameState;

import java.util.List;

/**
 * Interpreta un conjunto fijo de {@link Rule} contra un {@link GameState}: al recibir un
 * {@link Event}, evalúa las reglas cuyo tipo de evento coincide (y, si la regla viene de
 * {@code cards[].rules}, cuyo {@link Rule#sourceCardTemplateId()} coincide con la carta referida
 * por el evento), y para las que su condición se cumple, ejecuta la acción una vez por cada
 * elemento resuelto por el target. Devuelve si al menos una regla coincidió en tipo de evento y
 * tuvo condición verdadera — la señal genérica que usa {@code app} para distinguir un evento que
 * causó efecto de uno que no (p.ej. una jugada de un jugador que ninguna regla permite).
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
            if (rule.sourceCardTemplateId().isPresent() && !referencesCardTemplate(event, rule.sourceCardTemplateId().get())) {
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

    /**
     * Cast defensivo a propósito, a diferencia de las acciones/condiciones que leen
     * {@code event.payload("cardId")} (p.ej. {@code MoveCardAction}), que castean directo y
     * fallan alto si la config está mal. Aquí decidimos si una regla llega siquiera a evaluarse —
     * un payload ausente o de otro tipo debe traducirse en "esta regla no aplica", no en una
     * excepción.
     */
    private static boolean referencesCardTemplate(Event event, String templateId) {
        Object cardIdValue = event.payload("cardId");
        return cardIdValue instanceof String cardIdString && new CardId(cardIdString).templateId().equals(templateId);
    }
}
