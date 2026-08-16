package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Condition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionRegistryTest {

    @Test
    void registersAndReturnsCondition() {
        ConditionRegistry registry = new ConditionRegistry();
        Condition condition = ctx -> true;

        registry.register("CARD_TYPE_IS", condition);

        assertSame(condition, registry.get("CARD_TYPE_IS"));
        assertTrue(registry.contains("CARD_TYPE_IS"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ConditionRegistry registry = new ConditionRegistry();
        registry.register("CARD_TYPE_IS", ctx -> true);

        assertThrows(IllegalStateException.class,
                () -> registry.register("CARD_TYPE_IS", ctx -> false));
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
