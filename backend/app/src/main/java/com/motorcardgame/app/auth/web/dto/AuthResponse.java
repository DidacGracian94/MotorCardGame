package com.motorcardgame.app.auth.web.dto;

import com.motorcardgame.app.auth.application.AuthResult;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), UserResponse.from(result.user()));
    }
}
