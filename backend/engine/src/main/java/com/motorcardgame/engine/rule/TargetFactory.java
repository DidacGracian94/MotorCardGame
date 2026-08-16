package com.motorcardgame.engine.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.config.RuleSetParser;

/**
 * Construye un {@link Target} a partir del nodo JSON que lo configura. Recibe el
 * {@link RuleSetParser} por simetría con {@link ActionFactory}/{@link ConditionFactory}, aunque
 * los targets de este lote no lo necesiten todavía (no hay targets compuestos).
 */
@FunctionalInterface
public interface TargetFactory {

    Target create(JsonNode node, RuleSetParser parser);
}
