package com.motorcardgame.app.auth.application;

public class CannotChangeOwnRoleException extends RuntimeException {

    public CannotChangeOwnRoleException() {
        super("No puedes cambiar tu propio rol");
    }
}
