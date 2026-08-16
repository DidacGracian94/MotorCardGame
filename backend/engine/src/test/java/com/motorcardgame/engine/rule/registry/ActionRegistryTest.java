package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ActionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionRegistryTest {

    @Test
    void registersAndReturnsAction() {
        ActionRegistry registry = new ActionRegistry();
        ActionFactory factory = (node, parser) -> ctx -> {
        };

        registry.register("NEXT_PLAYER", factory);

        assertSame(factory, registry.get("NEXT_PLAYER"));
        assertTrue(registry.contains("NEXT_PLAYER"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ActionRegistry registry = new ActionRegistry();
        registry.register("NEXT_PLAYER", (node, parser) -> ctx -> {
        });

        assertThrows(IllegalStateException.class,
                () -> registry.register("NEXT_PLAYER", (node, parser) -> ctx -> {
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
