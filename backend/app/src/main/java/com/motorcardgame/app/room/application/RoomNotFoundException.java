package com.motorcardgame.app.room.application;

public final class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(String code) {
        super("Room not found: " + code);
    }
}
