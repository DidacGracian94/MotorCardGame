package com.motorcardgame.engine.rule;

import java.util.Objects;
import java.util.Optional;

/**
 * Vínculo EVENT → CONDITION → TARGET → ACTION: cuando ocurre un evento de tipo {@code eventType}
 * y {@code condition} se cumple, {@code action} se ejecuta una vez por cada elemento que resuelve
 * {@code target}.
 * <p>
 * Una regla declarada dentro de {@code cards[].rules} en la config lleva además
 * {@code sourceCardTemplateId}: el id de la plantilla de carta bajo la que se anidó. Con eso,
 * {@link RuleEngine} exige que el evento se refiera a esa carta en concreto (no solo al mismo
 * {@code eventType}) antes de evaluar siquiera la condición — el aislamiento entre cartas es así
 * estructural, no depende de que la condición de la regla lo compruebe por su cuenta. Una regla
 * del array global {@code "rules"} no tiene {@code sourceCardTemplateId} (vacío) y conserva el
 * comportamiento de siempre: solo se filtra por {@code eventType}.
 */
public final class Rule {

    private final String eventType;
    private final Condition condition;
    private final Target target;
    private final Action action;
    private final String sourceCardTemplateId;

    public Rule(String eventType, Condition condition, Target target, Action action) {
        this(eventType, condition, target, action, null);
    }

    public Rule(String eventType, Condition condition, Target target, Action action, String sourceCardTemplateId) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.condition = Objects.requireNonNull(condition, "condition");
        this.target = Objects.requireNonNull(target, "target");
        this.action = Objects.requireNonNull(action, "action");
        this.sourceCardTemplateId = sourceCardTemplateId;
    }

    public String eventType() {
        return eventType;
    }

    public Condition condition() {
        return condition;
    }

    public Target target() {
        return target;
    }

    public Action action() {
        return action;
    }

    public Optional<String> sourceCardTemplateId() {
        return Optional.ofNullable(sourceCardTemplateId);
    }
}
