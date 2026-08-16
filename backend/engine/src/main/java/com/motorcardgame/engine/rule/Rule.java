package com.motorcardgame.engine.rule;

import java.util.Objects;

/**
 * Vínculo EVENT → CONDITION → TARGET → ACTION: cuando ocurre un evento de tipo {@code eventType}
 * y {@code condition} se cumple, {@code action} se ejecuta una vez por cada elemento que resuelve
 * {@code target}.
 */
public final class Rule {

    private final String eventType;
    private final Condition condition;
    private final Target target;
    private final Action action;

    public Rule(String eventType, Condition condition, Target target, Action action) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.condition = Objects.requireNonNull(condition, "condition");
        this.target = Objects.requireNonNull(target, "target");
        this.action = Objects.requireNonNull(action, "action");
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
}
