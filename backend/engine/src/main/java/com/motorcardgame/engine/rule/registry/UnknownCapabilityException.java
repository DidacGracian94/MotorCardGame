package com.motorcardgame.engine.rule.registry;

/**
 * Se lanza al buscar una capacidad (acción, condición, ...) no registrada. Indica un error de
 * configuración/datos al interpretar una GameDefinition, no un caso que el caller deba manejar
 * con un flujo normal.
 */
public final class UnknownCapabilityException extends RuntimeException {

    public UnknownCapabilityException(String capabilityKind, String name) {
        super("Unknown " + capabilityKind + " capability: " + name);
    }
}
