package com.motorcardgame.app.auth.application;

import com.motorcardgame.app.auth.domain.User;

public record AuthResult(String accessToken, String refreshToken, User user) {
}
