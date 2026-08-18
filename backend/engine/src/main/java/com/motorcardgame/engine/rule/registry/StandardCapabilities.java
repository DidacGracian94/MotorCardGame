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
import com.motorcardgame.engine.rule.action.ReverseDirectionAction;
import com.motorcardgame.engine.rule.action.SequenceAction;
import com.motorcardgame.engine.rule.condition.AndCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeEqualsCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeMatchesZoneCondition;
import com.motorcardgame.engine.rule.condition.EventCardAttributeEqualsCondition;
import com.motorcardgame.engine.rule.condition.NotCondition;
import com.motorcardgame.engine.rule.condition.OrCondition;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.rule.condition.ZoneIsEmptyCondition;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldDescriptor;
import com.motorcardgame.engine.rule.target.AllPlayersTarget;
import com.motorcardgame.engine.rule.target.CurrentPlayerTarget;
import com.motorcardgame.engine.rule.target.NextPlayerTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Da de alta, en registries recién creados, las capacidades genéricas que trae el motor: los
 * decoradores lógicos/de control (AND, OR, NOT, SEQUENCE, REPEAT) y un lote de capacidades de
 * partida (DRAW_CARDS, NEXT_PLAYER, MOVE_CARD, REVERSE_DIRECTION, ZONE_IS_EMPTY,
 * CARD_ATTRIBUTE_EQUALS, CARD_ATTRIBUTE_MATCHES_ZONE, EVENT_CARD_ATTRIBUTE_EQUALS,
 * CURRENT_PLAYER, ALL_PLAYERS, NEXT_PLAYER) — generales para cualquier juego de cartas por turnos
 * con pilas y manos, no específicas de UNO, aunque UNO sea el primer caso que las ejercita.
 */
public final class StandardCapabilities {

    private static final List<String> POSITION_VALUES = List.of("TOP", "BOTTOM");

    private StandardCapabilities() {
    }

    public static void registerInto(
            ActionRegistry actionRegistry, ConditionRegistry conditionRegistry, TargetRegistry targetRegistry) {
        registerActions(actionRegistry);
        registerConditions(conditionRegistry);
        registerTargets(targetRegistry);
    }

    private static void registerActions(ActionRegistry actionRegistry) {
        actionRegistry.register(
                CapabilityDescriptor.action("DRAW_CARDS", List.of(
                        FieldDescriptor.zoneRef("from"),
                        FieldDescriptor.zoneRef("to"),
                        FieldDescriptor.integer("count"))),
                (node, parser) -> new DrawCardsAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to")),
                        JsonNodes.requiredInt(node, "count")));

        actionRegistry.register(
                CapabilityDescriptor.action("NEXT_PLAYER", List.of()),
                (node, parser) -> new NextPlayerAction());

        actionRegistry.register(
                CapabilityDescriptor.action("MOVE_CARD", List.of(
                        FieldDescriptor.zoneRef("from"),
                        FieldDescriptor.zoneRef("to"))),
                (node, parser) -> new MoveCardAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to"))));

        actionRegistry.register(
                CapabilityDescriptor.action("SEQUENCE", List.of(
                        FieldDescriptor.actionList("actions"))),
                (node, parser) -> {
                    List<Action> actions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "actions")) {
                        actions.add(parser.parseAction(child));
                    }
                    return new SequenceAction(actions.toArray(new Action[0]));
                });

        actionRegistry.register(
                CapabilityDescriptor.action("REPEAT", List.of(
                        FieldDescriptor.action("action"),
                        FieldDescriptor.integer("times"))),
                (node, parser) -> new RepeatAction(
                        parser.parseAction(JsonNodes.requiredObject(node, "action")),
                        JsonNodes.requiredInt(node, "times")));

        actionRegistry.register(
                CapabilityDescriptor.action("REVERSE_DIRECTION", List.of()),
                (node, parser) -> new ReverseDirectionAction());
    }

    private static void registerConditions(ConditionRegistry conditionRegistry) {
        conditionRegistry.register(
                CapabilityDescriptor.condition("ZONE_IS_EMPTY", List.of(
                        FieldDescriptor.zoneRef("zone"))),
                (node, parser) -> new ZoneIsEmptyCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("CARD_ATTRIBUTE_EQUALS", List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.enumField("position", POSITION_VALUES, "TOP"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.scalar("equals"))),
                (node, parser) -> new CardAttributeEqualsCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        readPosition(node),
                        JsonNodes.requiredText(node, "attribute"),
                        JsonNodes.scalarValue(node.path("equals"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("CARD_ATTRIBUTE_MATCHES_ZONE", List.of(
                        FieldDescriptor.zoneRef("cardZone"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.enumField("position", POSITION_VALUES, "TOP"))),
                (node, parser) -> new CardAttributeMatchesZoneCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "cardZone")),
                        JsonNodes.requiredText(node, "attribute"),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        readPosition(node)));

        conditionRegistry.register(
                CapabilityDescriptor.condition("EVENT_CARD_ATTRIBUTE_EQUALS", List.of(
                        FieldDescriptor.zoneRef("cardZone"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.scalar("equals"))),
                (node, parser) -> new EventCardAttributeEqualsCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "cardZone")),
                        JsonNodes.requiredText(node, "attribute"),
                        JsonNodes.scalarValue(node.path("equals"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("AND", List.of(
                        FieldDescriptor.conditionList("conditions"))),
                (node, parser) -> {
                    List<Condition> conditions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                        conditions.add(parser.parseCondition(child));
                    }
                    return new AndCondition(conditions.toArray(new Condition[0]));
                });

        conditionRegistry.register(
                CapabilityDescriptor.condition("OR", List.of(
                        FieldDescriptor.conditionList("conditions"))),
                (node, parser) -> {
                    List<Condition> conditions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                        conditions.add(parser.parseCondition(child));
                    }
                    return new OrCondition(conditions.toArray(new Condition[0]));
                });

        conditionRegistry.register(
                CapabilityDescriptor.condition("NOT", List.of(
                        FieldDescriptor.condition("condition"))),
                (node, parser) -> new NotCondition(
                        parser.parseCondition(JsonNodes.requiredObject(node, "condition"))));
    }

    private static void registerTargets(TargetRegistry targetRegistry) {
        targetRegistry.register(
                CapabilityDescriptor.target("CURRENT_PLAYER", List.of()),
                (node, parser) -> new CurrentPlayerTarget());
        targetRegistry.register(
                CapabilityDescriptor.target("ALL_PLAYERS", List.of()),
                (node, parser) -> new AllPlayersTarget());
        targetRegistry.register(
                CapabilityDescriptor.target("NEXT_PLAYER", List.of()),
                (node, parser) -> new NextPlayerTarget());
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
