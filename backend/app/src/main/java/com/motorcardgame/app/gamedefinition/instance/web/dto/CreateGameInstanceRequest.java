package com.motorcardgame.app.gamedefinition.instance.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateGameInstanceRequest(@NotNull @Min(1) Integer versionNumber) {
}
