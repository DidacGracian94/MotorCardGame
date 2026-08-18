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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Convierte el JSON de {@code GameDefinitionVersion.config} en {@link Rule} reales, resolviendo
 * cada nodo de condición/target/acción contra los registries de capacidades del motor. Es la
 * pieza que conecta la configuración declarativa (opaca para el motor hasta este punto) con
 * {@link com.motorcardgame.engine.rule.RuleEngine}. Un fallo al parsear (JSON mal formado, campo
 * obligatorio ausente, capacidad no registrada) es, por definición, una
 * {@code GameDefinition} inválida — se traduce siempre a {@link InvalidGameDefinitionException}.
 */
public final class RuleSetParser {

    /**
     * Único evento de ciclo de vida que dispara el propio motor/app (ver
     * {@code GameInstanceService.create}) — no requiere estar declarado en {@code playerActions}.
     */
    public static final String GAME_STARTED_EVENT = "GAME_STARTED";

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
        return parseRules(readRoot(configJson));
    }

    /**
     * Lee el vocabulario de eventos de usuario que esta {@code GameDefinition} declara
     * explícitamente (campo opcional {@code "playerActions"}). Ausente = ningún evento de usuario
     * declarado — solo {@link #GAME_STARTED_EVENT} sería válido en {@link #validateEvents}.
     */
    public Set<String> parsePlayerActions(String configJson) {
        return readPlayerActions(readRoot(configJson));
    }

    /**
     * Valida que el evento de cada regla sea {@link #GAME_STARTED_EVENT} o esté declarado en
     * {@code "playerActions"} — así una regla no puede quedar "muerta" por referenciar un nombre
     * de evento que nadie va a disparar nunca, ni un jugador puede disparar una acción que el
     * diseñador del juego no ha preparado. No se llama desde {@link #parse}: es una validación de
     * negocio adicional (invocada explícitamente al publicar una versión), no un requisito
     * estructural del JSON en sí.
     */
    public void validateEvents(String configJson) {
        JsonNode root = readRoot(configJson);
        Set<String> playerActions = readPlayerActions(root);
        for (Rule rule : parseRules(root)) {
            if (!isKnownEvent(rule.eventType(), playerActions)) {
                throw new InvalidGameDefinitionException(
                        "rule references unknown event \"" + rule.eventType()
                                + "\": must be \"" + GAME_STARTED_EVENT + "\" or declared in \"playerActions\"");
            }
        }
    }

    private static boolean isKnownEvent(String eventType, Set<String> playerActions) {
        return GAME_STARTED_EVENT.equals(eventType) || playerActions.contains(eventType);
    }

    private JsonNode readRoot(String configJson) {
        try {
            return objectMapper.readTree(configJson);
        } catch (JsonProcessingException e) {
            throw new InvalidGameDefinitionException("config is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    private Set<String> readPlayerActions(JsonNode root) {
        JsonNode node = root.path("playerActions");
        if (!node.isArray()) {
            return Set.of();
        }
        Set<String> actions = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                actions.add(item.asText());
            }
        }
        return actions;
    }

    /**
     * Junta las reglas del array global {@code "rules"} (obligatorio) con las que cuelgan de cada
     * carta en {@code "cards[].rules"} (opcional) — una regla declarada dentro de una carta no es
     * distinta en nada para el motor, solo vive en otro sitio del JSON para que el editor pueda
     * mostrarla pegada a "su" carta en vez de en la lista plana.
     */
    private List<Rule> parseRules(JsonNode root) {
        JsonNode rulesNode = JsonNodes.requiredArray(root, "rules");
        List<Rule> rules = new ArrayList<>();
        for (JsonNode ruleNode : rulesNode) {
            rules.add(parseRule(ruleNode));
        }
        rules.addAll(parseCardRules(root));
        return List.copyOf(rules);
    }

    private List<Rule> parseCardRules(JsonNode root) {
        List<Rule> rules = new ArrayList<>();
        JsonNode cardsNode = root.path("cards");
        if (!cardsNode.isArray()) {
            return rules;
        }
        for (JsonNode cardNode : cardsNode) {
            JsonNode cardRulesNode = cardNode.path("rules");
            if (!cardRulesNode.isArray()) {
                continue;
            }
            for (JsonNode ruleNode : cardRulesNode) {
                rules.add(parseRule(ruleNode));
            }
        }
        return rules;
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
