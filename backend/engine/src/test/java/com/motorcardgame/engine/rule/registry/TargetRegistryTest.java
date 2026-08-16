package com.motorcardgame.engine.rule.registry;

import com.motorcardgame.engine.rule.Target;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetRegistryTest {

    @Test
    void registersAndReturnsTarget() {
        TargetRegistry registry = new TargetRegistry();
        Target target = ctx -> List.of();

        registry.register("CURRENT_PLAYER", target);

        assertSame(target, registry.get("CURRENT_PLAYER"));
        assertTrue(registry.contains("CURRENT_PLAYER"));
    }

    @Test
    void rejectsDuplicateRegistration() {
        TargetRegistry registry = new TargetRegistry();
        registry.register("CURRENT_PLAYER", ctx -> List.of());

        assertThrows(IllegalStateException.class,
                () -> registry.register("CURRENT_PLAYER", ctx -> List.of()));
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
}
