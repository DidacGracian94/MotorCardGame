package com.motorcardgame.engine.rule.registry.describe;

/**
 * Distingue a qué registro pertenece una {@link CapabilityDescriptor}: acciones, condiciones o
 * targets.
 */
public enum CapabilityKind {
    ACTION,
    CONDITION,
    TARGET
}
