package com.motorcardgame.app.room.application;

public final class RoomNotOpenException extends RuntimeException {

    public RoomNotOpenException(String code) {
        super("Room is not open: " + code);
    }
}
