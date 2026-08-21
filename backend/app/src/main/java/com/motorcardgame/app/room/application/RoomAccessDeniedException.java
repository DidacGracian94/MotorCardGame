package com.motorcardgame.app.room.application;

public final class RoomAccessDeniedException extends RuntimeException {

    public RoomAccessDeniedException(String code) {
        super("Only the host can do this: " + code);
    }
}
