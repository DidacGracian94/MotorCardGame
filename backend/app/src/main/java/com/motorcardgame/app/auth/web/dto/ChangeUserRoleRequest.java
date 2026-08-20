package com.motorcardgame.app.auth.web.dto;

import com.motorcardgame.app.auth.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(@NotNull Role role) {
}
