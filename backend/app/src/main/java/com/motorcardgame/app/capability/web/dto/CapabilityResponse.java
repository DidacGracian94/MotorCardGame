package com.motorcardgame.app.capability.web.dto;

import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import java.util.List;

public record CapabilityResponse(String name, String description, List<CapabilityFieldResponse> fields) {

    public static CapabilityResponse from(CapabilityDescriptor descriptor) {
        return new CapabilityResponse(
                descriptor.name(),
                descriptor.description(),
                descriptor.fields().stream().map(CapabilityFieldResponse::from).toList());
    }
}
