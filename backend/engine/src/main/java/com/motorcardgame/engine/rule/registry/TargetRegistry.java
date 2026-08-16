package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Target;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro de capacidades {@link Target} disponibles para el motor, indexadas por nombre
 * (p.ej. "CURRENT_PLAYER").
 */
public final class TargetRegistry {

    private final Map<String, Target> targets = new HashMap<>();

    public void register(String name, Target target) {
        if (targets.containsKey(name)) {
            throw new IllegalStateException("Target already registered: " + name);
        }
        targets.put(name, target);
    }

    public Target get(String name) {
        Target target = targets.get(name);
        if (target == null) {
            throw new UnknownCapabilityException("target", name);
        }
        return target;
    }

    public boolean contains(String name) {
        return targets.containsKey(name);
    }
}
