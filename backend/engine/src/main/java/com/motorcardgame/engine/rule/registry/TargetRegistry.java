package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.TargetFactory;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro de factories de {@link com.motorcardgame.engine.rule.Target} disponibles para el
 * motor, indexadas por nombre (p.ej. "CURRENT_PLAYER"), junto con la metadata que las describe.
 */
public final class TargetRegistry {

    private final Map<String, TargetFactory> factories = new HashMap<>();
    private final Map<String, CapabilityDescriptor> descriptors = new LinkedHashMap<>();

    public void register(CapabilityDescriptor descriptor, TargetFactory factory) {
        String name = descriptor.name();
        if (factories.containsKey(name)) {
            throw new IllegalStateException("Target already registered: " + name);
        }
        factories.put(name, factory);
        descriptors.put(name, descriptor);
    }

    public TargetFactory get(String name) {
        TargetFactory factory = factories.get(name);
        if (factory == null) {
            throw new UnknownCapabilityException("target", name);
        }
        return factory;
    }

    public boolean contains(String name) {
        return factories.containsKey(name);
    }

    public CapabilityDescriptor describe(String name) {
        CapabilityDescriptor descriptor = descriptors.get(name);
        if (descriptor == null) {
            throw new UnknownCapabilityException("target", name);
        }
        return descriptor;
    }

    public Collection<CapabilityDescriptor> describeAll() {
        return List.copyOf(descriptors.values());
    }
}
