package com.motorcardgame.app.capability.web.dto;

import com.motorcardgame.engine.rule.registry.describe.FieldDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldKind;
import java.util.List;

public record CapabilityFieldResponse(
        String name, FieldKind kind, boolean required, String defaultValue, List<String> enumValues) {

    public static CapabilityFieldResponse from(FieldDescriptor descriptor) {
        return new CapabilityFieldResponse(
                descriptor.name(),
                descriptor.kind(),
                descriptor.required(),
                descriptor.defaultValue(),
                descriptor.enumValues());
    }
}
