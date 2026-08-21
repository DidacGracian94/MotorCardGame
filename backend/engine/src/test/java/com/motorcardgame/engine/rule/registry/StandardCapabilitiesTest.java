package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardCapabilitiesTest {

    @Test
    void registersAllStandardCapabilities() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();

        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        assertTrue(actionRegistry.contains("DRAW_CARDS"));
        assertTrue(actionRegistry.contains("NEXT_PLAYER"));
        assertTrue(actionRegistry.contains("MOVE_CARD"));
        assertTrue(actionRegistry.contains("SEQUENCE"));
        assertTrue(actionRegistry.contains("REPEAT"));
        assertTrue(actionRegistry.contains("REVERSE_DIRECTION"));
        assertTrue(actionRegistry.contains("ADD_POINTS"));
        assertTrue(actionRegistry.contains("MOVE_ALL_CARDS"));
        assertTrue(actionRegistry.contains("SET_CURRENT_PLAYER"));
        assertTrue(actionRegistry.contains("REMEMBER_CARD_ATTRIBUTE"));

        assertTrue(conditionRegistry.contains("ZONE_IS_EMPTY"));
        assertTrue(conditionRegistry.contains("CARD_ATTRIBUTE_EQUALS"));
        assertTrue(conditionRegistry.contains("CARD_ATTRIBUTE_MATCHES_ZONE"));
        assertTrue(conditionRegistry.contains("ANY_CARD_ATTRIBUTE_MATCHES_ZONE"));
        assertTrue(conditionRegistry.contains("EVENT_CARD_ATTRIBUTE_EQUALS"));
        assertTrue(conditionRegistry.contains("EVENT_CARD_IN_ZONE"));
        assertTrue(conditionRegistry.contains("AND"));
        assertTrue(conditionRegistry.contains("OR"));
        assertTrue(conditionRegistry.contains("NOT"));
        assertTrue(conditionRegistry.contains("ZONE_CARD_COUNT_EQUALS"));

        assertTrue(targetRegistry.contains("CURRENT_PLAYER"));
        assertTrue(targetRegistry.contains("ALL_PLAYERS"));
        assertTrue(targetRegistry.contains("NEXT_PLAYER"));
        assertTrue(targetRegistry.contains("WINNING_CARD_OWNER"));

        assertEquals(10, actionRegistry.describeAll().size());
        assertEquals(10, conditionRegistry.describeAll().size());
        assertEquals(4, targetRegistry.describeAll().size());
    }

    @Test
    void drawCardsHasThreeFieldsWithExpectedKinds() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        CapabilityDescriptor descriptor = actionRegistry.describe("DRAW_CARDS");

        assertEquals(3, descriptor.fields().size());
        assertEquals(List.of(FieldKind.ZONE_REF, FieldKind.ZONE_REF, FieldKind.INTEGER),
                descriptor.fields().stream().map(FieldDescriptor::kind).toList());
        assertEquals(List.of("from", "to", "count"),
                descriptor.fields().stream().map(FieldDescriptor::name).toList());
    }

    @Test
    void cardAttributeEqualsPositionFieldIsOptionalEnumWithDefault() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        CapabilityDescriptor descriptor = conditionRegistry.describe("CARD_ATTRIBUTE_EQUALS");
        FieldDescriptor position = descriptor.fields().stream()
                .filter(field -> field.name().equals("position"))
                .findFirst()
                .orElseThrow();

        assertEquals(FieldKind.ENUM, position.kind());
        assertFalse(position.required());
        assertEquals("TOP", position.defaultValue());
        assertEquals(List.of("TOP", "BOTTOM"), position.enumValues());
    }

    @Test
    void andAndOrHaveExactlyOneConditionListField() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        for (String name : List.of("AND", "OR")) {
            CapabilityDescriptor descriptor = conditionRegistry.describe(name);
            assertEquals(1, descriptor.fields().size());
            assertEquals(FieldKind.CONDITION_LIST, descriptor.fields().get(0).kind());
        }
    }

    @Test
    void notHasExactlyOneConditionField() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        CapabilityDescriptor descriptor = conditionRegistry.describe("NOT");

        assertEquals(1, descriptor.fields().size());
        assertEquals(FieldKind.CONDITION, descriptor.fields().get(0).kind());
    }

    @Test
    void sequenceHasExactlyOneActionListField() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        CapabilityDescriptor descriptor = actionRegistry.describe("SEQUENCE");

        assertEquals(1, descriptor.fields().size());
        assertEquals(FieldKind.ACTION_LIST, descriptor.fields().get(0).kind());
    }

    @Test
    void repeatHasAnActionFieldAndAnIntegerField() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);

        CapabilityDescriptor descriptor = actionRegistry.describe("REPEAT");

        assertEquals(2, descriptor.fields().size());
        assertEquals(List.of(FieldKind.ACTION, FieldKind.INTEGER),
                descriptor.fields().stream().map(FieldDescriptor::kind).toList());
    }
}
