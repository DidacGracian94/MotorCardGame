package com.motorcardgame.app.room.web.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequest(@NotBlank String displayName) {
}
