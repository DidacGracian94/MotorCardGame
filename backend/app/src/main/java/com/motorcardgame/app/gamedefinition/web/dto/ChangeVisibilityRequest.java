package com.motorcardgame.app.gamedefinition.web.dto;

import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
import jakarta.validation.constraints.NotNull;

public record ChangeVisibilityRequest(@NotNull GameDefinitionVisibility visibility) {
}
