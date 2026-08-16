package com.motorcardgame.engine.rule;

import java.util.List;

/**
 * Capacidad genérica que resuelve a qué elementos (jugadores, cartas, zonas, ...) se aplica una
 * {@link Action}, dado un {@link RuleContext}. Devuelve una lista sin tipar porque un mismo
 * {@link com.motorcardgame.engine.rule.registry.TargetRegistry} debe poder resolver a tipos de
 * elemento distintos según el target — la misma compensación tipo-seguridad-por-genericidad que
 * ya asumen {@link com.motorcardgame.engine.rule.registry.ActionRegistry} y
 * {@link com.motorcardgame.engine.rule.registry.ConditionRegistry}.
 */
@FunctionalInterface
public interface Target {

    List<Object> resolve(RuleContext context);
}
