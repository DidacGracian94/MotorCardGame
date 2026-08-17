package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ConditionFactory;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro de factories de {@link com.motorcardgame.engine.rule.Condition} disponibles para el
 * motor, indexadas por nombre (p.ej. "CARD_ATTRIBUTE_EQUALS"), junto con la metadata que las
 * describe.
 */
public final class ConditionRegistry {

    private final Map<String, ConditionFactory> factories = new HashMap<>();
    private final Map<String, CapabilityDescriptor> descriptors = new LinkedHashMap<>();

    public void register(CapabilityDescriptor descriptor, ConditionFactory factory) {
        String name = descriptor.name();
        if (factories.containsKey(name)) {
            throw new IllegalStateException("Condition already registered: " + name);
        }
        factories.put(name, factory);
        descriptors.put(name, descriptor);
    }

    public ConditionFactory get(String name) {
        ConditionFactory factory = factories.get(name);
        if (factory == null) {
            throw new UnknownCapabilityException("condition", name);
        }
        return factory;
    }

    public boolean contains(String name) {
        return factories.containsKey(name);
    }

    public CapabilityDescriptor describe(String name) {
        CapabilityDescriptor descriptor = descriptors.get(name);
        if (descriptor == null) {
            throw new UnknownCapabilityException("condition", name);
        }
        return descriptor;
    }

    public Collection<CapabilityDescriptor> describeAll() {
        return List.copyOf(descriptors.values());
    }
}
