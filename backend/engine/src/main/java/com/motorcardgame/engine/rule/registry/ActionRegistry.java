package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de capacidades {@link Action} disponibles para el motor, indexadas por nombre
 * (p.ej. "DRAW_CARDS").
 */
public final class ActionRegistry {

    private final Map<String, Action> actions = new HashMap<>();

    public void register(String name, Action action) {
        if (actions.containsKey(name)) {
            throw new IllegalStateException("Action already registered: " + name);
        }
        actions.put(name, action);
    }

    public Action get(String name) {
        Action action = actions.get(name);
        if (action == null) {
            throw new UnknownCapabilityException("action", name);
        }
        return action;
    }

    public boolean contains(String name) {
        return actions.containsKey(name);
    }
}
