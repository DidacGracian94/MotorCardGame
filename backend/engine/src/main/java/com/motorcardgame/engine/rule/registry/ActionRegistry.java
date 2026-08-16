package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ActionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de factories de {@link com.motorcardgame.engine.rule.Action} disponibles para el
 * motor, indexadas por nombre (p.ej. "DRAW_CARDS").
 */
public final class ActionRegistry {

    private final Map<String, ActionFactory> actions = new HashMap<>();

    public void register(String name, ActionFactory factory) {
        if (actions.containsKey(name)) {
            throw new IllegalStateException("Action already registered: " + name);
        }
        actions.put(name, factory);
    }

    public ActionFactory get(String name) {
        ActionFactory factory = actions.get(name);
        if (factory == null) {
            throw new UnknownCapabilityException("action", name);
        }
        return factory;
    }

    public boolean contains(String name) {
        return actions.containsKey(name);
    }
}
