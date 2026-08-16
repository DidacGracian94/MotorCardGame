package com.motorcardgame.engine.rule;

/**
 * Capacidad genérica que evalúa una condición sobre un {@link RuleContext}. Interfaz de un solo
 * método para que decoradores (AND/OR/NOT) puedan envolver otras condiciones sin necesidad de
 * crear una capacidad nueva por cada combinación.
 */
@FunctionalInterface
public interface Condition {

    boolean evaluate(RuleContext context);
}
