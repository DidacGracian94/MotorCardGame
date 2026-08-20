package com.motorcardgame.app.auth.application;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Ya existe una cuenta con email: " + email);
    }
}
