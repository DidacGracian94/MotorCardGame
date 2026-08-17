package com.motorcardgame.engine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.Rule;
import com.motorcardgame.engine.rule.Target;
import com.motorcardgame.engine.rule.registry.ActionRegistry;
import com.motorcardgame.engine.rule.registry.ConditionRegistry;
import com.motorcardgame.engine.rule.registry.TargetRegistry;
import com.motorcardgame.engine.rule.registry.UnknownCapabilityException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte el JSON de {@code GameDefinitionVersion.config} en {@link Rule} reales, resolviendo
 * cada nodo de condición/target/acción contra los registries de capacidades del motor. Es la
 * pieza que conecta la configuración declarativa (opaca para el motor hasta este punto) con
 * {@link com.motorcardgame.engine.rule.RuleEngine}. Un fallo al parsear (JSON mal formado, campo
 * obligatorio ausente, capacidad no registrada) es, por definición, una
 * {@code GameDefinition} inválida — se traduce siempre a {@link InvalidGameDefinitionException}.
 */
public final class RuleSetParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ActionRegistry actionRegistry;
    private final ConditionRegistry conditionRegistry;
    private final TargetRegistry targetRegistry;

    public RuleSetParser(
            ActionRegistry actionRegistry, ConditionRegistry conditionRegistry, TargetRegistry targetRegistry) {
        this.actionRegistry = Objects.requireNonNull(actionRegistry, "actionRegistry");
        this.conditionRegistry = Objects.requireNonNull(conditionRegistry, "conditionRegistry");
        this.targetRegistry = Objects.requireNonNull(targetRegistry, "targetRegistry");
    }

    public List<Rule> parse(String configJson) {
        JsonNode root;
        try {
            root = objectMapper.readTree(configJson);
        } catch (JsonProcessingException e) {
            throw new InvalidGameDefinitionException("config is not valid JSON: " + e.getOriginalMessage(), e);
        }
        JsonNode rulesNode = JsonNodes.requiredArray(root, "rules");
        List<Rule> rules = new ArrayList<>();
        for (JsonNode ruleNode : rulesNode) {
            rules.add(parseRule(ruleNode));
        }
        return List.copyOf(rules);
    }

    private Rule parseRule(JsonNode node) {
        String eventType = JsonNodes.requiredText(node, "event");
        Condition condition = parseCondition(JsonNodes.requiredObject(node, "condition"));
        Target target = parseTarget(JsonNodes.requiredObject(node, "target"));
        Action action = parseAction(JsonNodes.requiredObject(node, "action"));
        return new Rule(eventType, condition, target, action);
    }

    public Condition parseCondition(JsonNode node) {
        String type = JsonNodes.requiredText(node, "type");
        try {
            return conditionRegistry.get(type).create(node, this);
        } catch (UnknownCapabilityException e) {
            throw new InvalidGameDefinitionException(e.getMessage(), e);
        }
    }

    public Target parseTarget(JsonNode node) {
        String type = JsonNodes.requiredText(node, "type");
        try {
            return targetRegistry.get(type).create(node, this);
        } catch (UnknownCapabilityException e) {
            throw new InvalidGameDefinitionException(e.getMessage(), e);
        }
    }

    public Action parseAction(JsonNode node) {
        String type = JsonNodes.requiredText(node, "type");
        try {
            return actionRegistry.get(type).create(node, this);
        } catch (UnknownCapabilityException e) {
            throw new InvalidGameDefinitionException(e.getMessage(), e);
        }
    }

    public ActionRegistry actionRegistry() {
        return actionRegistry;
    }

    public ConditionRegistry conditionRegistry() {
        return conditionRegistry;
    }

    public TargetRegistry targetRegistry() {
        return targetRegistry;
    }
}
