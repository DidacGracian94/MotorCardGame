package com.motorcardgame.app.auth.application;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido, caducado o revocado");
    }
}
