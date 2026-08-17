package com.motorcardgame.engine.rule.registry.describe;

/**
 * Tipo de un campo de configuración de una capacidad (acción/condición/target), tal y como se
 * expone por {@code /api/capabilities} para que un editor visual sepa qué control renderizar.
 */
public enum FieldKind {
    ZONE_REF,
    TEXT,
    INTEGER,
    BOOLEAN,
    SCALAR,
    ENUM,
    CONDITION,
    CONDITION_LIST,
    ACTION,
    ACTION_LIST
}
