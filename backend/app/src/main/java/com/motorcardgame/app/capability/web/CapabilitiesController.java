package com.motorcardgame.app.capability.web;

import com.motorcardgame.app.capability.web.dto.CapabilitiesResponse;
import com.motorcardgame.app.capability.web.dto.CapabilityResponse;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import java.util.Collection;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el catálogo de capacidades (acciones, condiciones, targets) que el motor conoce, con su
 * metadata de campos, para que un editor visual de {@code GameDefinition} pueda construir su UI
 * sin conocer cada capacidad de antemano.
 */
@RestController
@RequestMapping("/api/capabilities")
public class CapabilitiesController {

    private final RuleSetParser ruleSetParser;

    public CapabilitiesController(RuleSetParser ruleSetParser) {
        this.ruleSetParser = ruleSetParser;
    }

    @GetMapping
    public CapabilitiesResponse list() {
        return new CapabilitiesResponse(
                toResponses(ruleSetParser.actionRegistry().describeAll()),
                toResponses(ruleSetParser.conditionRegistry().describeAll()),
                toResponses(ruleSetParser.targetRegistry().describeAll()));
    }

    private static List<CapabilityResponse> toResponses(Collection<CapabilityDescriptor> descriptors) {
        return descriptors.stream().map(CapabilityResponse::from).toList();
    }
}
