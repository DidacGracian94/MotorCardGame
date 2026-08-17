package com.motorcardgame.engine.rule.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.config.JsonNodes;
import com.motorcardgame.engine.config.ZoneRefs;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.action.DrawCardsAction;
import com.motorcardgame.engine.rule.action.MoveCardAction;
import com.motorcardgame.engine.rule.action.NextPlayerAction;
import com.motorcardgame.engine.rule.action.RepeatAction;
import com.motorcardgame.engine.rule.action.SequenceAction;
import com.motorcardgame.engine.rule.condition.AndCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeEqualsCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeMatchesZoneCondition;
import com.motorcardgame.engine.rule.condition.NotCondition;
import com.motorcardgame.engine.rule.condition.OrCondition;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.rule.condition.ZoneIsEmptyCondition;
import com.motorcardgame.engine.rule.target.AllPlayersTarget;
import com.motorcardgame.engine.rule.target.CurrentPlayerTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Da de alta, en registries recién creados, las capacidades genéricas que trae el motor: los
 * decoradores lógicos/de control (AND, OR, NOT, SEQUENCE, REPEAT) y un primer lote de
 * capacidades de partida (DRAW_CARDS, NEXT_PLAYER, ZONE_IS_EMPTY, CARD_ATTRIBUTE_EQUALS,
 * CURRENT_PLAYER, ALL_PLAYERS) — generales para cualquier juego de cartas por turnos con pilas y
 * manos, no específicas de UNO, aunque UNO sea el primer caso que las ejercita.
 */
public final class StandardCapabilities {

    private StandardCapabilities() {
    }

    public static void registerInto(
            ActionRegistry actionRegistry, ConditionRegistry conditionRegistry, TargetRegistry targetRegistry) {
        registerActions(actionRegistry);
        registerConditions(conditionRegistry);
        registerTargets(targetRegistry);
    }

    private static void registerActions(ActionRegistry actionRegistry) {
        actionRegistry.register("DRAW_CARDS", (node, parser) -> new DrawCardsAction(
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to")),
                JsonNodes.requiredInt(node, "count")));

        actionRegistry.register("NEXT_PLAYER", (node, parser) -> new NextPlayerAction());

        actionRegistry.register("MOVE_CARD", (node, parser) -> new MoveCardAction(
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to"))));

        actionRegistry.register("SEQUENCE", (node, parser) -> {
            List<Action> actions = new ArrayList<>();
            for (JsonNode child : JsonNodes.requiredArray(node, "actions")) {
                actions.add(parser.parseAction(child));
            }
            return new SequenceAction(actions.toArray(new Action[0]));
        });

        actionRegistry.register("REPEAT", (node, parser) -> new RepeatAction(
                parser.parseAction(JsonNodes.requiredObject(node, "action")),
                JsonNodes.requiredInt(node, "times")));
    }

    private static void registerConditions(ConditionRegistry conditionRegistry) {
        conditionRegistry.register("ZONE_IS_EMPTY", (node, parser) -> new ZoneIsEmptyCondition(
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone"))));

        conditionRegistry.register("CARD_ATTRIBUTE_EQUALS", (node, parser) -> new CardAttributeEqualsCondition(
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                readPosition(node),
                JsonNodes.requiredText(node, "attribute"),
                JsonNodes.scalarValue(node.path("equals"))));

        conditionRegistry.register("CARD_ATTRIBUTE_MATCHES_ZONE", (node, parser) -> new CardAttributeMatchesZoneCondition(
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "cardZone")),
                JsonNodes.requiredText(node, "attribute"),
                ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                readPosition(node)));

        conditionRegistry.register("AND", (node, parser) -> {
            List<Condition> conditions = new ArrayList<>();
            for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                conditions.add(parser.parseCondition(child));
            }
            return new AndCondition(conditions.toArray(new Condition[0]));
        });

        conditionRegistry.register("OR", (node, parser) -> {
            List<Condition> conditions = new ArrayList<>();
            for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                conditions.add(parser.parseCondition(child));
            }
            return new OrCondition(conditions.toArray(new Condition[0]));
        });

        conditionRegistry.register("NOT", (node, parser) -> new NotCondition(
                parser.parseCondition(JsonNodes.requiredObject(node, "condition"))));
    }

    private static void registerTargets(TargetRegistry targetRegistry) {
        targetRegistry.register("CURRENT_PLAYER", (node, parser) -> new CurrentPlayerTarget());
        targetRegistry.register("ALL_PLAYERS", (node, parser) -> new AllPlayersTarget());
    }

    private static Position readPosition(JsonNode node) {
        JsonNode positionNode = node.path("position");
        if (positionNode.isMissingNode() || positionNode.isNull()) {
            return Position.TOP;
        }
        if (!positionNode.isTextual()) {
            throw new InvalidGameDefinitionException("field \"position\" must be text: " + node);
        }
        try {
            return Position.valueOf(positionNode.asText());
        } catch (IllegalArgumentException e) {
            throw new InvalidGameDefinitionException("unknown position \"" + positionNode.asText() + "\": " + node, e);
        }
    }
}
