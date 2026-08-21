package com.motorcardgame.app.room.web.dto;

import com.motorcardgame.app.room.domain.GameRoom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoomResponse(
        String code,
        UUID gameDefinitionId,
        int versionNumber,
        UUID hostUserId,
        String status,
        List<RoomPlayerResponse> players,
        UUID instanceId,
        Instant createdAt) {

    public record RoomPlayerResponse(String id, String displayName) {
    }

    public static RoomResponse from(GameRoom room) {
        List<RoomPlayerResponse> players = room.players().stream()
                .map(p -> new RoomPlayerResponse(p.id(), p.displayName()))
                .toList();
        return new RoomResponse(
                room.code(),
                room.gameDefinitionId(),
                room.versionNumber(),
                room.hostUserId(),
                room.status().name(),
                players,
                room.instanceId(),
                room.createdAt());
    }
}
