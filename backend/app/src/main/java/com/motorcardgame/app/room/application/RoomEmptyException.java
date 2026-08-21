package com.motorcardgame.app.room.application;

public final class RoomEmptyException extends RuntimeException {

    public RoomEmptyException(String code) {
        super("Room has no players to start: " + code);
    }
}
