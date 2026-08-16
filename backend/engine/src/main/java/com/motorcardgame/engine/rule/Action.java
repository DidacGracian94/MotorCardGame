package com.motorcardgame.engine.rule;

/**
 * Capacidad genérica que ejecuta un efecto sobre un {@link RuleContext}. Interfaz de un solo
 * método para que decoradores (SEQUENCE/REPEAT) puedan envolver otras acciones sin necesidad de
 * crear una capacidad nueva por cada combinación.
 */
@FunctionalInterface
public interface Action {

    void execute(RuleContext context);
}
