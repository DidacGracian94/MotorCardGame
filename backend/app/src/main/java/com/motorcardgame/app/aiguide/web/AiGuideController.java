package com.motorcardgame.app.aiguide.web;

import com.motorcardgame.app.aiguide.web.dto.AiGuideResponse;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.rule.registry.describe.CapabilityDescriptor;
import com.motorcardgame.engine.rule.registry.describe.FieldDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Genera una guía en Markdown — plantilla estática de conceptos + referencia de capacidades
 * generada en vivo desde los mismos registries que expone {@code /api/capabilities} — para que el
 * usuario la use como prompt en un chat de IA externo (Claude/ChatGPT/Gemini) y obtenga un JSON de
 * {@code GameDefinition} válido. No llama a ninguna IA: solo construye el texto que el usuario
 * copia y pega él mismo, así el coste de tokens lo asume su propia cuenta.
 */
@RestController
@RequestMapping("/api/ai-guide")
public class AiGuideController {

    private static final String TEMPLATE_RESOURCE = "ai-guide-template.md";

    private final RuleSetParser ruleSetParser;

    public AiGuideController(RuleSetParser ruleSetParser) {
        this.ruleSetParser = ruleSetParser;
    }

    @GetMapping
    public AiGuideResponse get() {
        return new AiGuideResponse(readTemplate() + "\n\n" + renderCapabilities());
    }

    private String readTemplate() {
        try (var input = new ClassPathResource(TEMPLATE_RESOURCE).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("could not load " + TEMPLATE_RESOURCE, e);
        }
    }

    private String renderCapabilities() {
        StringBuilder out = new StringBuilder();
        appendSection(out, "Acciones (`action.type`)", ruleSetParser.actionRegistry().describeAll());
        appendSection(out, "Condiciones (`condition.type`)", ruleSetParser.conditionRegistry().describeAll());
        appendSection(out, "Targets (`target.type`)", ruleSetParser.targetRegistry().describeAll());
        return out.toString();
    }

    private void appendSection(StringBuilder out, String title, Collection<CapabilityDescriptor> descriptors) {
        out.append("### ").append(title).append("\n\n");
        for (CapabilityDescriptor descriptor : descriptors) {
            appendCapability(out, descriptor);
        }
    }

    private void appendCapability(StringBuilder out, CapabilityDescriptor descriptor) {
        out.append("#### `").append(descriptor.name()).append("`\n\n");
        if (!descriptor.description().isBlank()) {
            out.append(descriptor.description()).append("\n\n");
        }
        List<FieldDescriptor> fields = descriptor.fields();
        if (fields.isEmpty()) {
            out.append("Sin campos adicionales.\n\n");
            return;
        }
        out.append("| campo | tipo | obligatorio | valor por defecto | valores posibles |\n");
        out.append("|---|---|---|---|---|\n");
        for (FieldDescriptor field : fields) {
            out.append("| ").append(field.name())
                    .append(" | ").append(field.kind())
                    .append(" | ").append(field.required() ? "sí" : "no")
                    .append(" | ").append(field.defaultValue() == null ? "-" : field.defaultValue())
                    .append(" | ").append(field.enumValues() == null ? "-" : String.join(", ", field.enumValues()))
                    .append(" |\n");
        }
        out.append("\n");
    }
}
