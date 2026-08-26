package com.motorcardgame.engine.rule.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.motorcardgame.engine.config.JsonNodes;
import com.motorcardgame.engine.config.ZoneRefs;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.rule.Action;
import com.motorcardgame.engine.rule.Condition;
import com.motorcardgame.engine.rule.action.AddPointsAction;
import com.motorcardgame.engine.rule.action.DeclareWinnerAction;
import com.motorcardgame.engine.rule.action.DrawCardsAction;
import com.motorcardgame.engine.rule.action.MoveAllCardsAction;
import com.motorcardgame.engine.rule.action.MoveCardAction;
import com.motorcardgame.engine.rule.action.NextPlayerAction;
import com.motorcardgame.engine.rule.action.RememberCardAttributeAction;
import com.motorcardgame.engine.rule.action.RepeatAction;
import com.motorcardgame.engine.rule.action.ReverseDirectionAction;
import com.motorcardgame.engine.rule.action.SequenceAction;
import com.motorcardgame.engine.rule.action.SetCurrentPlayerAction;
import com.motorcardgame.engine.rule.condition.AndCondition;
import com.motorcardgame.engine.rule.condition.AnyCardAttributeMatchesZoneCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeEqualsCondition;
import com.motorcardgame.engine.rule.condition.CardAttributeMatchesZoneCondition;
import com.motorcardgame.engine.rule.condition.EventCardAttributeEqualsCondition;
import com.motorcardgame.engine.rule.condition.EventCardInZoneCondition;
import com.motorcardgame.engine.rule.condition.NotCondition;
import com.motorcardgame.engine.rule.condition.OrCondition;
import com.motorcardgame.engine.rule.condition.Position;
import com.motorcardgame.engine.rule.condition.ZoneCardCountEqualsCondition;
import com.motorcardgame.engine.rule.condition.ZoneIsEmptyCondition;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldDescriptor;
import com.motorcardgame.engine.rule.target.AllPlayersTarget;
import com.motorcardgame.engine.rule.target.CurrentPlayerTarget;
import com.motorcardgame.engine.rule.target.NextPlayerTarget;
import com.motorcardgame.engine.rule.target.WinningCardOwnerTarget;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Da de alta, en registries recién creados, las capacidades genéricas que trae el motor: los
 * decoradores lógicos/de control (AND, OR, NOT, SEQUENCE, REPEAT) y un lote de capacidades de
 * partida (DRAW_CARDS, NEXT_PLAYER, MOVE_CARD, REVERSE_DIRECTION, ZONE_IS_EMPTY,
 * CARD_ATTRIBUTE_EQUALS, CARD_ATTRIBUTE_MATCHES_ZONE, ANY_CARD_ATTRIBUTE_MATCHES_ZONE,
 * EVENT_CARD_ATTRIBUTE_EQUALS, EVENT_CARD_IN_ZONE, CURRENT_PLAYER, ALL_PLAYERS, NEXT_PLAYER) —
 * generales para cualquier juego
 * de cartas por turnos con pilas y manos, no específicas de UNO, aunque UNO sea el primer caso que
 * las ejercita.
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
                CapabilityDescriptor.action("DRAW_CARDS",
                        "Mueve \"count\" cartas del tope de la zona \"from\" al tope de la zona \"to\".",
                        List.of(
                        FieldDescriptor.zoneRef("from"),
                        FieldDescriptor.zoneRef("to"),
                        FieldDescriptor.integer("count"))),
                (node, parser) -> new DrawCardsAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to")),
                        JsonNodes.requiredInt(node, "count")));

        actionRegistry.register(
                CapabilityDescriptor.action("NEXT_PLAYER",
                        "Avanza el turno al siguiente jugador según el sentido actual de la partida.",
                        List.of()),
                (node, parser) -> new NextPlayerAction());

        actionRegistry.register(
                CapabilityDescriptor.action("MOVE_CARD",
                        "Mueve la carta identificada por \"cardId\" en el evento desde \"from\" al tope de \"to\";"
                                + " opcionalmente graba quién la jugó en el atributo \"stampOwnerAs\".",
                        List.of(
                        FieldDescriptor.zoneRef("from"),
                        FieldDescriptor.zoneRef("to"),
                        FieldDescriptor.optionalText("stampOwnerAs"))),
                (node, parser) -> new MoveCardAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to")),
                        JsonNodes.optionalText(node, "stampOwnerAs", null)));

        actionRegistry.register(
                CapabilityDescriptor.action("SEQUENCE",
                        "Ejecuta una lista de \"actions\" en orden, una tras otra.",
                        List.of(
                        FieldDescriptor.actionList("actions"))),
                (node, parser) -> {
                    List<Action> actions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "actions")) {
                        actions.add(parser.parseAction(child));
                    }
                    return new SequenceAction(actions.toArray(new Action[0]));
                });

        actionRegistry.register(
                CapabilityDescriptor.action("REPEAT",
                        "Ejecuta \"action\" un número fijo (\"times\") de veces.",
                        List.of(
                        FieldDescriptor.action("action"),
                        FieldDescriptor.integer("times"))),
                (node, parser) -> new RepeatAction(
                        parser.parseAction(JsonNodes.requiredObject(node, "action")),
                        JsonNodes.requiredInt(node, "times")));

        actionRegistry.register(
                CapabilityDescriptor.action("REVERSE_DIRECTION",
                        "Invierte el sentido en el que avanza el turno entre jugadores.",
                        List.of()),
                (node, parser) -> new ReverseDirectionAction());

        actionRegistry.register(
                CapabilityDescriptor.action("ADD_POINTS",
                        "Suma, al jugador target, el atributo numérico \"attribute\" de todas las cartas de"
                                + " \"zone\" como puntuación.",
                        List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.text("attribute"))),
                (node, parser) -> new AddPointsAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        JsonNodes.requiredText(node, "attribute")));

        actionRegistry.register(
                CapabilityDescriptor.action("MOVE_ALL_CARDS",
                        "Mueve todas las cartas de \"from\" a \"to\", sin importar cuántas haya.",
                        List.of(
                        FieldDescriptor.zoneRef("from"),
                        FieldDescriptor.zoneRef("to"))),
                (node, parser) -> new MoveAllCardsAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "from")),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "to"))));

        actionRegistry.register(
                CapabilityDescriptor.action("SET_CURRENT_PLAYER",
                        "Cede el turno directamente al target resuelto por la regla (p.ej. el ganador de una"
                                + " baza), sin respetar el orden de asiento.",
                        List.of()),
                (node, parser) -> new SetCurrentPlayerAction());

        actionRegistry.register(
                CapabilityDescriptor.action("DECLARE_WINNER",
                        "Declara ganador al target resuelto por la regla y marca la partida como terminada"
                                + " (p.ej. CURRENT_PLAYER al vaciar la mano en UNO).",
                        List.of()),
                (node, parser) -> new DeclareWinnerAction());

        actionRegistry.register(
                CapabilityDescriptor.action("REMEMBER_CARD_ATTRIBUTE",
                        "Lee el atributo \"attribute\" de la carta en \"position\" de \"zone\" y lo guarda como"
                                + " variable de partida bajo el nombre \"as\", para poder usarlo después aunque"
                                + " esa carta cambie de zona.",
                        List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.enumField("position", POSITION_VALUES, "TOP"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.text("as"))),
                (node, parser) -> new RememberCardAttributeAction(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        readPosition(node),
                        JsonNodes.requiredText(node, "attribute"),
                        JsonNodes.requiredText(node, "as")));
    }

    private static void registerConditions(ConditionRegistry conditionRegistry) {
        conditionRegistry.register(
                CapabilityDescriptor.condition("ZONE_IS_EMPTY",
                        "Comprueba si \"zone\" no tiene ninguna carta.",
                        List.of(
                        FieldDescriptor.zoneRef("zone"))),
                (node, parser) -> new ZoneIsEmptyCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("CARD_ATTRIBUTE_EQUALS",
                        "Compara el atributo \"attribute\" de la carta en \"position\" de \"zone\" contra el"
                                + " valor \"equals\".",
                        List.of(
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
                CapabilityDescriptor.condition("CARD_ATTRIBUTE_MATCHES_ZONE",
                        "Compara el atributo \"attribute\" de la carta del evento (en \"cardZone\") contra el"
                                + " mismo atributo de la carta en \"position\" de \"zone\".",
                        List.of(
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
                CapabilityDescriptor.condition("ANY_CARD_ATTRIBUTE_MATCHES_ZONE",
                        "Comprueba si alguna carta de \"zone\" comparte el valor de \"attribute\" con la carta"
                                + " en \"referencePosition\" de \"referenceZone\" — p.ej. \"tengo alguna carta de"
                                + " ese palo/color en la mano\".",
                        List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.zoneRef("referenceZone"),
                        FieldDescriptor.enumField("referencePosition", POSITION_VALUES, "TOP"))),
                (node, parser) -> new AnyCardAttributeMatchesZoneCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        JsonNodes.requiredText(node, "attribute"),
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "referenceZone")),
                        readPosition(node, "referencePosition")));

        conditionRegistry.register(
                CapabilityDescriptor.condition("EVENT_CARD_ATTRIBUTE_EQUALS",
                        "Compara el atributo \"attribute\" de la carta del evento (en \"cardZone\") contra el"
                                + " valor \"equals\".",
                        List.of(
                        FieldDescriptor.zoneRef("cardZone"),
                        FieldDescriptor.text("attribute"),
                        FieldDescriptor.scalar("equals"))),
                (node, parser) -> new EventCardAttributeEqualsCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "cardZone")),
                        JsonNodes.requiredText(node, "attribute"),
                        JsonNodes.scalarValue(node.path("equals"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("EVENT_CARD_IN_ZONE",
                        "Comprueba si la carta identificada por \"cardId\" en el evento está actualmente en"
                                + " \"zone\".",
                        List.of(
                        FieldDescriptor.zoneRef("zone"))),
                (node, parser) -> new EventCardInZoneCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("AND",
                        "Se cumple si todas las \"conditions\" de la lista se cumplen.",
                        List.of(
                        FieldDescriptor.conditionList("conditions"))),
                (node, parser) -> {
                    List<Condition> conditions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                        conditions.add(parser.parseCondition(child));
                    }
                    return new AndCondition(conditions.toArray(new Condition[0]));
                });

        conditionRegistry.register(
                CapabilityDescriptor.condition("OR",
                        "Se cumple si al menos una de las \"conditions\" de la lista se cumple.",
                        List.of(
                        FieldDescriptor.conditionList("conditions"))),
                (node, parser) -> {
                    List<Condition> conditions = new ArrayList<>();
                    for (JsonNode child : JsonNodes.requiredArray(node, "conditions")) {
                        conditions.add(parser.parseCondition(child));
                    }
                    return new OrCondition(conditions.toArray(new Condition[0]));
                });

        conditionRegistry.register(
                CapabilityDescriptor.condition("NOT",
                        "Invierte el resultado de \"condition\".",
                        List.of(
                        FieldDescriptor.condition("condition"))),
                (node, parser) -> new NotCondition(
                        parser.parseCondition(JsonNodes.requiredObject(node, "condition"))));

        conditionRegistry.register(
                CapabilityDescriptor.condition("ZONE_CARD_COUNT_EQUALS",
                        "Comprueba si \"zone\" tiene exactamente \"count\" cartas.",
                        List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.integer("count"))),
                (node, parser) -> new ZoneCardCountEqualsCondition(
                        ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                        JsonNodes.requiredInt(node, "count")));
    }

    private static void registerTargets(TargetRegistry targetRegistry) {
        targetRegistry.register(
                CapabilityDescriptor.target("CURRENT_PLAYER",
                        "Resuelve al jugador cuyo turno está en curso.",
                        List.of()),
                (node, parser) -> new CurrentPlayerTarget());
        targetRegistry.register(
                CapabilityDescriptor.target("ALL_PLAYERS",
                        "Resuelve a todos los jugadores de la partida.",
                        List.of()),
                (node, parser) -> new AllPlayersTarget());
        targetRegistry.register(
                CapabilityDescriptor.target("NEXT_PLAYER",
                        "Resuelve al jugador al que le tocaría a continuación (respetando el sentido de turno"
                                + " actual), sin avanzar el turno.",
                        List.of()),
                (node, parser) -> new NextPlayerTarget());
        targetRegistry.register(
                CapabilityDescriptor.target("WINNING_CARD_OWNER",
                        "Resuelve al jugador dueño de la carta con mayor \"rankAttribute\" dentro del grupo"
                                + " preferido (p.ej. el palo de triunfo) en \"zone\"; si ninguna carta pertenece a"
                                + " ese grupo, gana la carta más fuerte del grupo que abrió la ronda — genérico"
                                + " para resolver bazas en juegos de cartas por turnos.",
                        List.of(
                        FieldDescriptor.zoneRef("zone"),
                        FieldDescriptor.text("rankAttribute"),
                        FieldDescriptor.text("groupAttribute"),
                        FieldDescriptor.optionalZoneRef("preferredGroupZone"),
                        FieldDescriptor.enumField("preferredGroupPosition", POSITION_VALUES, "TOP"),
                        FieldDescriptor.text("ownerAttribute"),
                        FieldDescriptor.optionalText("preferredGroupVariable"))),
                (node, parser) -> {
                    ZoneRef preferredGroupZone = ZoneRefs.optionalFromJson(node, "preferredGroupZone");
                    String preferredGroupVariable = JsonNodes.optionalText(node, "preferredGroupVariable", null);
                    if (preferredGroupZone == null && preferredGroupVariable == null) {
                        throw new InvalidGameDefinitionException(
                                "WINNING_CARD_OWNER requires \"preferredGroupZone\" or \"preferredGroupVariable\": " + node);
                    }
                    return new WinningCardOwnerTarget(
                            ZoneRefs.fromJson(JsonNodes.requiredObject(node, "zone")),
                            JsonNodes.requiredText(node, "rankAttribute"),
                            JsonNodes.requiredText(node, "groupAttribute"),
                            preferredGroupZone,
                            readPosition(node, "preferredGroupPosition"),
                            JsonNodes.requiredText(node, "ownerAttribute"),
                            preferredGroupVariable);
                });
    }

    private static Position readPosition(JsonNode node) {
        return readPosition(node, "position");
    }

    private static Position readPosition(JsonNode node, String fieldName) {
        JsonNode positionNode = node.path(fieldName);
        if (positionNode.isMissingNode() || positionNode.isNull()) {
            return Position.TOP;
        }
        if (!positionNode.isTextual()) {
            throw new InvalidGameDefinitionException("field \"" + fieldName + "\" must be text: " + node);
        }
        try {
            return Position.valueOf(positionNode.asText());
        } catch (IllegalArgumentException e) {
            throw new InvalidGameDefinitionException(
                    "unknown " + fieldName + " \"" + positionNode.asText() + "\": " + node, e);
        }
    }
}
