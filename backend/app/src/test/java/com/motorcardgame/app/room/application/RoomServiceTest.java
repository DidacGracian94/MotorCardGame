package com.motorcardgame.app.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.motorcardgame.app.auth.application.JwtService;
import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.room.domain.GameRoom;
import com.motorcardgame.app.room.domain.RoomPlayer;
import com.motorcardgame.app.room.domain.RoomRepository;
import com.motorcardgame.engine.state.Player;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final UUID GAME_DEFINITION_ID = UUID.randomUUID();
    private static final UUID HOST_USER_ID = UUID.randomUUID();
    private static final Role HOST_ROLE = Role.USER;

    @Mock
    private RoomRepository repository;

    @Mock
    private GameDefinitionVersionService gameDefinitionVersionService;

    @Mock
    private GameInstanceService gameInstanceService;

    @Mock
    private JwtService jwtService;

    private RoomService service;

    @BeforeEach
    void setUp() {
        service = new RoomService(repository, gameDefinitionVersionService, gameInstanceService, jwtService);
    }

    @Test
    void create_persistsRoomWithGeneratedCode_whenVersionVisibleToHost() {
        when(gameDefinitionVersionService.getByVersionNumber(GAME_DEFINITION_ID, 1, HOST_USER_ID, HOST_ROLE))
                .thenReturn(GameDefinitionVersion.publish(GAME_DEFINITION_ID, 1, "{}"));

        GameRoom room = service.create(GAME_DEFINITION_ID, 1, HOST_USER_ID, HOST_ROLE);

        assertThat(room.code()).hasSize(6);
        assertThat(room.gameDefinitionId()).isEqualTo(GAME_DEFINITION_ID);
        assertThat(room.hostUserId()).isEqualTo(HOST_USER_ID);
        verify(repository).save(room);
    }

    @Test
    void create_propagatesVersionNotFound_whenVersionMissing() {
        when(gameDefinitionVersionService.getByVersionNumber(GAME_DEFINITION_ID, 1, HOST_USER_ID, HOST_ROLE))
                .thenThrow(new com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionNotFoundException(
                        GAME_DEFINITION_ID, 1));

        assertThatThrownBy(() -> service.create(GAME_DEFINITION_ID, 1, HOST_USER_ID, HOST_ROLE))
                .isInstanceOf(com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void join_addsPlayerAndIssuesGuestAccessToken_whenRoomOpen() {
        GameRoom room = GameRoom.create("ABC123", GAME_DEFINITION_ID, 1, HOST_USER_ID);
        when(repository.findByCode("ABC123")).thenReturn(Optional.of(room));
        when(jwtService.issueAccessToken(any(UUID.class), eq(Role.USER))).thenReturn("guest-jwt");

        RoomService.RoomJoinResult result = service.join("abc123", "Alice");

        assertThat(result.player().displayName()).isEqualTo("Alice");
        assertThat(result.accessToken()).isEqualTo("guest-jwt");
        assertThat(room.players()).extracting(RoomPlayer::displayName).containsExactly("Alice");
    }

    @Test
    void join_throwsRoomNotFound_whenCodeUnknown() {
        when(repository.findByCode("ZZZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.join("zzzzzz", "Alice")).isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void join_throwsRoomNotOpen_whenRoomAlreadyStarted() {
        GameRoom room = GameRoom.create("ABC123", GAME_DEFINITION_ID, 1, HOST_USER_ID);
        room.join("Alice");
        room.start(UUID.randomUUID());
        when(repository.findByCode("ABC123")).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.join("ABC123", "Bob")).isInstanceOf(RoomNotOpenException.class);
    }

    @Test
    void start_createsInstanceFromJoinedPlayersAndMarksRoomStarted_whenCalledByHost() {
        GameRoom room = GameRoom.create("ABC123", GAME_DEFINITION_ID, 3, HOST_USER_ID);
        room.join("Alice");
        room.join("Bob");
        when(repository.findByCode("ABC123")).thenReturn(Optional.of(room));
        GameInstance instance = GameInstance.create(GAME_DEFINITION_ID, UUID.randomUUID(), "{}");
        when(gameInstanceService.create(eq(GAME_DEFINITION_ID), eq(3), any(), eq(HOST_USER_ID), eq(HOST_ROLE)))
                .thenReturn(instance);

        GameRoom started = service.start("ABC123", HOST_USER_ID, HOST_ROLE);

        assertThat(started.status().name()).isEqualTo("STARTED");
        assertThat(started.instanceId()).isEqualTo(instance.id());
        ArgumentCaptor<List<Player>> playersCaptor = ArgumentCaptor.forClass(List.class);
        verify(gameInstanceService).create(eq(GAME_DEFINITION_ID), anyInt(), playersCaptor.capture(), eq(HOST_USER_ID), eq(HOST_ROLE));
        assertThat(playersCaptor.getValue()).hasSize(2);
    }

    @Test
    void start_throwsAccessDenied_whenCallerIsNotHost() {
        GameRoom room = GameRoom.create("ABC123", GAME_DEFINITION_ID, 1, HOST_USER_ID);
        room.join("Alice");
        lenient().when(repository.findByCode("ABC123")).thenReturn(Optional.of(room));

        UUID someoneElse = UUID.randomUUID();
        assertThatThrownBy(() -> service.start("ABC123", someoneElse, Role.USER))
                .isInstanceOf(RoomAccessDeniedException.class);
        verify(gameInstanceService, never()).create(any(), anyInt(), any(), any(), any());
    }

    @Test
    void start_throwsRoomEmpty_withoutCreatingAnInstance_whenNoPlayersJoined() {
        GameRoom room = GameRoom.create("ABC123", GAME_DEFINITION_ID, 1, HOST_USER_ID);
        when(repository.findByCode("ABC123")).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.start("ABC123", HOST_USER_ID, HOST_ROLE))
                .isInstanceOf(RoomEmptyException.class);
        verify(gameInstanceService, never()).create(any(), anyInt(), any(), any(), any());
    }
}
