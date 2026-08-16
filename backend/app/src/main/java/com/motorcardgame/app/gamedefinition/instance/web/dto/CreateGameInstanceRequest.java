package com.motorcardgame.app.gamedefinition.instance.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateGameInstanceRequest(
        @NotNull @Min(1) Integer versionNumber, @NotEmpty @Valid List<PlayerRequest> players) {

    public record PlayerRequest(@NotBlank String id, @NotBlank String displayName) {
    }
}
