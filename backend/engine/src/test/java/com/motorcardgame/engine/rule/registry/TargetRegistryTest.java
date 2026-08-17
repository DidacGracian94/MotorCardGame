package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.TargetFactory;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetRegistryTest {

    @Test
    void registersAndReturnsTarget() {
        TargetRegistry registry = new TargetRegistry();
        TargetFactory factory = (node, parser) -> ctx -> List.of();

        registry.register(CapabilityDescriptor.target("CURRENT_PLAYER", List.of()), factory);

        assertSame(factory, registry.get("CURRENT_PLAYER"));
        assertTrue(registry.contains("CURRENT_PLAYER"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        TargetRegistry registry = new TargetRegistry();
        registry.register(CapabilityDescriptor.target("CURRENT_PLAYER", List.of()), (node, parser) -> ctx -> List.of());

        assertThrows(IllegalStateException.class,
                () -> registry.register(CapabilityDescriptor.target("CURRENT_PLAYER", List.of()), (node, parser) -> ctx -> List.of()));
    }

    @Test
    void throwsOnUnknownTarget() {
        TargetRegistry registry = new TargetRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.get("UNKNOWN"));
    }

    @Test
    void containsReturnsFalseForUnknownTarget() {
        TargetRegistry registry = new TargetRegistry();

        assertFalse(registry.contains("UNKNOWN"));
    }

    @Test
    void describeReturnsRegisteredDescriptor() {
        TargetRegistry registry = new TargetRegistry();
        CapabilityDescriptor descriptor = CapabilityDescriptor.target("CURRENT_PLAYER", List.of());
        registry.register(descriptor, (node, parser) -> ctx -> List.of());

        assertSame(descriptor, registry.describe("CURRENT_PLAYER"));
    }

    @Test
    void describeAllPreservesRegistrationOrder() {
        TargetRegistry registry = new TargetRegistry();
        registry.register(CapabilityDescriptor.target("CURRENT_PLAYER", List.of()), (node, parser) -> ctx -> List.of());
        registry.register(CapabilityDescriptor.target("ALL_PLAYERS", List.of()), (node, parser) -> ctx -> List.of());

        assertEquals(List.of("CURRENT_PLAYER", "ALL_PLAYERS"),
                registry.describeAll().stream().map(CapabilityDescriptor::name).toList());
    }

    @Test
    void describeThrowsOnUnknownCapability() {
        TargetRegistry registry = new TargetRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.describe("UNKNOWN"));
    }
}
