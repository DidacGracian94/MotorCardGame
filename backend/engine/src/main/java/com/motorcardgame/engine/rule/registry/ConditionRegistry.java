package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Condition;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de capacidades {@link Condition} disponibles para el motor, indexadas por nombre
 * (p.ej. "CARD_TYPE_IS").
 */
public final class ConditionRegistry {

    private final Map<String, Condition> conditions = new HashMap<>();

    public void register(String name, Condition condition) {
        if (conditions.containsKey(name)) {
            throw new IllegalStateException("Condition already registered: " + name);
        }
        conditions.put(name, condition);
    }

    public Condition get(String name) {
        Condition condition = conditions.get(name);
        if (condition == null) {
            throw new UnknownCapabilityException("condition", name);
        }
        return condition;
    }

    public boolean contains(String name) {
        return conditions.containsKey(name);
    }
}
