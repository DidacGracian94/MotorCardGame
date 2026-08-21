package com.motorcardgame.app.room.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRoomRequest(@NotNull UUID gameDefinitionId, @NotNull @Min(1) Integer versionNumber) {
}
