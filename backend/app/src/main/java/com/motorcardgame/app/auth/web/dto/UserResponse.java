package com.motorcardgame.app.auth.web.dto;

import com.motorcardgame.app.auth.domain.User;
import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.email(), user.displayName());
    }
}
