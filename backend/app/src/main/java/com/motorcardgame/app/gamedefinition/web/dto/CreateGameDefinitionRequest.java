package com.motorcardgame.app.gamedefinition.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateGameDefinitionRequest(
        @NotNull UUID ownerId,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "slug debe ser minúsculas, números y guiones (ej: mi-juego-2)")
        String slug) {
}
