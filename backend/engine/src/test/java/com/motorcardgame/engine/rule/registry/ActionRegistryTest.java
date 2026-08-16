package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Action;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionRegistryTest {

    @Test
    void registersAndReturnsAction() {
        ActionRegistry registry = new ActionRegistry();
        Action action = ctx -> {
        };

        registry.register("NEXT_PLAYER", action);

        assertSame(action, registry.get("NEXT_PLAYER"));
        assertTrue(registry.contains("NEXT_PLAYER"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ActionRegistry registry = new ActionRegistry();
        registry.register("NEXT_PLAYER", ctx -> {
        });

        assertThrows(IllegalStateException.class,
                () -> registry.register("NEXT_PLAYER", ctx -> {
                }));
    }

    @Test
    void throwsOnUnknownAction() {
        ActionRegistry registry = new ActionRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.get("UNKNOWN"));
    }

    @Test
    void containsReturnsFalseForUnknownAction() {
        ActionRegistry registry = new ActionRegistry();

        assertFalse(registry.contains("UNKNOWN"));
    }
}
