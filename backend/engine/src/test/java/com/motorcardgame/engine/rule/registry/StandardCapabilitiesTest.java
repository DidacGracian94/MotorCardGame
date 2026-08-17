package com.motorcardgame.engine.rule.registry;

import org.junit.jupiter.api.Test;

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

        assertTrue(conditionRegistry.contains("ZONE_IS_EMPTY"));
        assertTrue(conditionRegistry.contains("CARD_ATTRIBUTE_EQUALS"));
        assertTrue(conditionRegistry.contains("CARD_ATTRIBUTE_MATCHES_ZONE"));
        assertTrue(conditionRegistry.contains("AND"));
        assertTrue(conditionRegistry.contains("OR"));
        assertTrue(conditionRegistry.contains("NOT"));

        assertTrue(targetRegistry.contains("CURRENT_PLAYER"));
        assertTrue(targetRegistry.contains("ALL_PLAYERS"));
    }
}
