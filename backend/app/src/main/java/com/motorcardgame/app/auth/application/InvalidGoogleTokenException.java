package com.motorcardgame.app.auth.application;

public class InvalidGoogleTokenException extends RuntimeException {

    public InvalidGoogleTokenException() {
        super("Token de Google inválido");
    }

    public InvalidGoogleTokenException(Throwable cause) {
        super("Token de Google inválido", cause);
    }
}
