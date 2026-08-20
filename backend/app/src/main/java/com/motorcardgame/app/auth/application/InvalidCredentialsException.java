package com.motorcardgame.app.auth.application;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }
}
