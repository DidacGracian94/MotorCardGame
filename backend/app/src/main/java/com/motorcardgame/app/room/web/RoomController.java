package com.motorcardgame.app.room.web;

import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.room.application.RoomService;
import com.motorcardgame.app.room.domain.GameRoom;
import com.motorcardgame.app.room.web.dto.CreateRoomRequest;
import com.motorcardgame.app.room.web.dto.JoinRoomRequest;
import com.motorcardgame.app.room.web.dto.JoinRoomResponse;
import com.motorcardgame.app.room.web.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code create} y {@code start} exigen sesión (el host) — el resto de {@code /api/**} ya lo pide
 * por defecto. {@code get} y {@code join} quedan abiertos en {@code SecurityConfig} porque un
 * jugador que se une a una sala todavía no tiene ningún token.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody CreateRoomRequest request) {
        TokenClaims principal = currentPrincipal();
        GameRoom room = roomService.create(
                request.gameDefinitionId(), request.versionNumber(), principal.userId(), principal.role());
        return RoomResponse.from(room);
    }

    @GetMapping("/{code}")
    public RoomResponse get(@PathVariable String code) {
        return RoomResponse.from(roomService.getByCode(code));
    }

    @PostMapping("/{code}/join")
    public JoinRoomResponse join(@PathVariable String code, @Valid @RequestBody JoinRoomRequest request) {
        RoomService.RoomJoinResult result = roomService.join(code, request.displayName());
        return new JoinRoomResponse(
                result.player().id(), result.player().displayName(), result.accessToken(), RoomResponse.from(result.room()));
    }

    @PostMapping("/{code}/start")
    public RoomResponse start(@PathVariable String code) {
        TokenClaims principal = currentPrincipal();
        GameRoom room = roomService.start(code, principal.userId(), principal.role());
        return RoomResponse.from(room);
    }

    private static TokenClaims currentPrincipal() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
