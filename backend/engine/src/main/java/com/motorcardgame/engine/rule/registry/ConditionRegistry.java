package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ConditionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de factories de {@link com.motorcardgame.engine.rule.Condition} disponibles para el
 * motor, indexadas por nombre (p.ej. "CARD_ATTRIBUTE_EQUALS").
 */
public final class ConditionRegistry {

    private final Map<String, ConditionFactory> conditions = new HashMap<>();

    public void register(String name, ConditionFactory factory) {
        if (conditions.containsKey(name)) {
            throw new IllegalStateException("Condition already registered: " + name);
        }
        conditions.put(name, factory);
    }

    public ConditionFactory get(String name) {
        ConditionFactory factory = conditions.get(name);
        if (factory == null) {
            throw new UnknownCapabilityException("condition", name);
        }
        return factory;
    }

    public boolean contains(String name) {
        return conditions.containsKey(name);
    }
}
