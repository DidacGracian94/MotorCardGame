package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ActionFactory;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        registry.register(CapabilityDescriptor.action("NEXT_PLAYER", List.of()), factory);

        assertSame(factory, registry.get("NEXT_PLAYER"));
        assertTrue(registry.contains("NEXT_PLAYER"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ActionRegistry registry = new ActionRegistry();
        registry.register(CapabilityDescriptor.action("NEXT_PLAYER", List.of()), (node, parser) -> ctx -> {
        });

        assertThrows(IllegalStateException.class,
                () -> registry.register(CapabilityDescriptor.action("NEXT_PLAYER", List.of()), (node, parser) -> ctx -> {
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

    @Test
    void describeReturnsRegisteredDescriptor() {
        ActionRegistry registry = new ActionRegistry();
        CapabilityDescriptor descriptor = CapabilityDescriptor.action("NEXT_PLAYER", List.of());
        registry.register(descriptor, (node, parser) -> ctx -> {
        });

        assertSame(descriptor, registry.describe("NEXT_PLAYER"));
    }

    @Test
    void describeAllPreservesRegistrationOrder() {
        ActionRegistry registry = new ActionRegistry();
        registry.register(CapabilityDescriptor.action("NEXT_PLAYER", List.of()), (node, parser) -> ctx -> {
        });
        registry.register(CapabilityDescriptor.action("SEQUENCE", List.of()), (node, parser) -> ctx -> {
        });

        assertEquals(List.of("NEXT_PLAYER", "SEQUENCE"),
                registry.describeAll().stream().map(CapabilityDescriptor::name).toList());
    }

    @Test
    void describeThrowsOnUnknownCapability() {
        ActionRegistry registry = new ActionRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.describe("UNKNOWN"));
    }
}
