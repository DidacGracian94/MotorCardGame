package com.motorcardgame.engine.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.config.RuleSetParser;

/**
 * Construye una {@link Condition} a partir del nodo JSON que la configura. Recibe el
 * {@link RuleSetParser} para que las capacidades compuestas (p.ej. AND, OR, NOT) puedan parsear
 * recursivamente las condiciones que envuelven, sin que {@link
 * com.motorcardgame.engine.rule.registry.ConditionRegistry} necesite conocer el parser.
 */
@FunctionalInterface
public interface ConditionFactory {

    Condition create(JsonNode node, RuleSetParser parser);
}
