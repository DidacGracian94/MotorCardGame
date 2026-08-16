package com.motorcardgame.app.gamedefinition.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameGameDefinitionRequest(@NotBlank @Size(max = 120) String name) {
}
