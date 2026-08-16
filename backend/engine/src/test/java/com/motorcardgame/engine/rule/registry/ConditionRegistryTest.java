package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ConditionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionRegistryTest {

    @Test
    void registersAndReturnsCondition() {
        ConditionRegistry registry = new ConditionRegistry();
        ConditionFactory factory = (node, parser) -> ctx -> true;

        registry.register("CARD_TYPE_IS", factory);

        assertSame(factory, registry.get("CARD_TYPE_IS"));
        assertTrue(registry.contains("CARD_TYPE_IS"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ConditionRegistry registry = new ConditionRegistry();
        registry.register("CARD_TYPE_IS", (node, parser) -> ctx -> true);

        assertThrows(IllegalStateException.class,
                () -> registry.register("CARD_TYPE_IS", (node, parser) -> ctx -> false));
    }

    @Test
    void throwsOnUnknownCondition() {
        ConditionRegistry registry = new ConditionRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.get("UNKNOWN"));
    }

    @Test
    void containsReturnsFalseForUnknownCondition() {
        ConditionRegistry registry = new ConditionRegistry();

        assertFalse(registry.contains("UNKNOWN"));
    }
}
