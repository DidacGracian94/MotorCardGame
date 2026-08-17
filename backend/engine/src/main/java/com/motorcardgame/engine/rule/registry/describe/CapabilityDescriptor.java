package com.motorcardgame.engine.rule.registry.describe;

import java.util.List;

/**
 * Describe una capacidad (acción, condición o target) registrada en el motor: su nombre JSON
 * (p.ej. "DRAW_CARDS") y la lista de campos que espera en su configuración. Es la metadata que
 * expone {@code /api/capabilities} para que un editor visual pueda construir su UI sin conocer
 * cada capacidad de antemano.
 */
public record CapabilityDescriptor(String name, CapabilityKind kind, List<FieldDescriptor> fields) {

    public static CapabilityDescriptor action(String name, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.ACTION, fields);
    }

    public static CapabilityDescriptor condition(String name, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.CONDITION, fields);
    }

    public static CapabilityDescriptor target(String name, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.TARGET, fields);
    }
}
