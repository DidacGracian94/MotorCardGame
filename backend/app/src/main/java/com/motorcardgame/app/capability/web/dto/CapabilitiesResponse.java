package com.motorcardgame.app.capability.web.dto;

import java.util.List;

public record CapabilitiesResponse(
        List<CapabilityResponse> actions, List<CapabilityResponse> conditions, List<CapabilityResponse> targets) {
}
