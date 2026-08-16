package com.motorcardgame.engine.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.config.RuleSetParser;

/**
 * Construye una {@link Action} a partir del nodo JSON que la configura. Recibe el
 * {@link RuleSetParser} para que las capacidades compuestas (p.ej. SEQUENCE, REPEAT) puedan
 * parsear recursivamente las acciones que envuelven, sin que {@link
 * com.motorcardgame.engine.rule.registry.ActionRegistry} necesite conocer el parser.
 */
@FunctionalInterface
public interface ActionFactory {

    Action create(JsonNode node, RuleSetParser parser);
}
