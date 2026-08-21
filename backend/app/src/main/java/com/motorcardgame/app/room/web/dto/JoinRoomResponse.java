package com.motorcardgame.app.room.web.dto;

public record JoinRoomResponse(String playerId, String displayName, String accessToken, RoomResponse room) {
}
