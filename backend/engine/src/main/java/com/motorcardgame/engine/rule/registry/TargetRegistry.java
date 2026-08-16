package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.TargetFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de factories de {@link com.motorcardgame.engine.rule.Target} disponibles para el
 * motor, indexadas por nombre (p.ej. "CURRENT_PLAYER").
 */
public final class TargetRegistry {

    private final Map<String, TargetFactory> targets = new HashMap<>();

    public void register(String name, TargetFactory factory) {
        if (targets.containsKey(name)) {
            throw new IllegalStateException("Target already registered: " + name);
        }
        targets.put(name, factory);
    }

    public TargetFactory get(String name) {
        TargetFactory factory = targets.get(name);
        if (factory == null) {
            throw new UnknownCapabilityException("target", name);
        }
        return factory;
    }

    public boolean contains(String name) {
        return targets.containsKey(name);
    }
}
