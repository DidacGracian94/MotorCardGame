package com.motorcardgame.app.auth.application;

import com.motorcardgame.app.auth.domain.Role;
import java.util.UUID;

public record TokenClaims(UUID userId, Role role) {
}
