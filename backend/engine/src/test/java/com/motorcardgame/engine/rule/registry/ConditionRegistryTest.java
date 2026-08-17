package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.ConditionFactory;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionRegistryTest {

    @Test
    void registersAndReturnsCondition() {
        ConditionRegistry registry = new ConditionRegistry();
        ConditionFactory factory = (node, parser) -> ctx -> true;

        registry.register(CapabilityDescriptor.condition("CARD_TYPE_IS", List.of()), factory);

        assertSame(factory, registry.get("CARD_TYPE_IS"));
        assertTrue(registry.contains("CARD_TYPE_IS"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        ConditionRegistry registry = new ConditionRegistry();
        registry.register(CapabilityDescriptor.condition("CARD_TYPE_IS", List.of()), (node, parser) -> ctx -> true);

        assertThrows(IllegalStateException.class,
                () -> registry.register(CapabilityDescriptor.condition("CARD_TYPE_IS", List.of()), (node, parser) -> ctx -> false));
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

    @Test
    void describeReturnsRegisteredDescriptor() {
        ConditionRegistry registry = new ConditionRegistry();
        CapabilityDescriptor descriptor = CapabilityDescriptor.condition("CARD_TYPE_IS", List.of());
        registry.register(descriptor, (node, parser) -> ctx -> true);

        assertSame(descriptor, registry.describe("CARD_TYPE_IS"));
    }

    @Test
    void describeAllPreservesRegistrationOrder() {
        ConditionRegistry registry = new ConditionRegistry();
        registry.register(CapabilityDescriptor.condition("AND", List.of()), (node, parser) -> ctx -> true);
        registry.register(CapabilityDescriptor.condition("OR", List.of()), (node, parser) -> ctx -> true);

        assertEquals(List.of("AND", "OR"),
                registry.describeAll().stream().map(CapabilityDescriptor::name).toList());
    }

    @Test
    void describeThrowsOnUnknownCapability() {
        ConditionRegistry registry = new ConditionRegistry();

        assertThrows(UnknownCapabilityException.class, () -> registry.describe("UNKNOWN"));
    }
}
