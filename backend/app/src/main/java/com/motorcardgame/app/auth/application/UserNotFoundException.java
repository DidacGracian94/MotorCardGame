package com.motorcardgame.app.auth.application;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Usuario no encontrado: " + id);
    }
}
