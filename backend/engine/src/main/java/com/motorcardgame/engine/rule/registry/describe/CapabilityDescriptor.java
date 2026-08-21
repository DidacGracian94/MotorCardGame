package com.motorcardgame.engine.rule.registry.describe;

import java.util.List;

/**
 * Describe una capacidad (acción, condición o target) registrada en el motor: su nombre JSON
 * (p.ej. "DRAW_CARDS"), una descripción en lenguaje natural de qué hace, y la lista de campos que
 * espera en su configuración. Es la metadata que expone {@code /api/capabilities} (para que un
 * editor visual pueda construir su UI sin conocer cada capacidad de antemano) y {@code
 * /api/ai-guide} (para generar una referencia legible por una IA externa).
 *
 * <p>{@code description} es opcional (cadena vacía si no se indica) — solo hace falta para las
 * capacidades que realmente se registran en el motor (ver {@link
 * com.motorcardgame.engine.rule.registry.StandardCapabilities}); los tests unitarios de los
 * registries pueden seguir usando los factory methods de 2 argumentos sin tener que inventarse una
 * descripción.
 */
public record CapabilityDescriptor(String name, CapabilityKind kind, String description, List<FieldDescriptor> fields) {

    public static CapabilityDescriptor action(String name, List<FieldDescriptor> fields) {
        return action(name, "", fields);
    }

    public static CapabilityDescriptor action(String name, String description, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.ACTION, description, fields);
    }

    public static CapabilityDescriptor condition(String name, List<FieldDescriptor> fields) {
        return condition(name, "", fields);
    }

    public static CapabilityDescriptor condition(String name, String description, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.CONDITION, description, fields);
    }

    public static CapabilityDescriptor target(String name, List<FieldDescriptor> fields) {
        return target(name, "", fields);
    }

    public static CapabilityDescriptor target(String name, String description, List<FieldDescriptor> fields) {
        return new CapabilityDescriptor(name, CapabilityKind.TARGET, description, fields);
    }
}
