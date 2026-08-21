package com.motorcardgame.app.room.domain;

import com.motorcardgame.app.room.application.RoomEmptyException;
import com.motorcardgame.app.room.application.RoomNotOpenException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sala de espera estilo Kahoot: un host crea la sala para una {@code GameDefinitionVersion}
 * concreta, los jugadores se unen con solo un nombre (sin cuenta) hasta que el host la arranca,
 * momento en el que se convierte en una {@code GameInstance} real. Vive solo en memoria
 * ({@code InMemoryRoomRepository}) — no sobrevive a un reinicio del backend, asumible para el
 * tiempo de vida corto de una sala. Los métodos que leen o mutan la lista de jugadores están
 * sincronizados sobre la propia instancia porque varios jugadores pueden unirse a la vez.
 */
public final class GameRoom {

    private final String code;
    private final UUID gameDefinitionId;
    private final int versionNumber;
    private final UUID hostUserId;
    private final Instant createdAt;
    private final List<RoomPlayer> players = new ArrayList<>();
    private RoomStatus status = RoomStatus.OPEN;
    private UUID instanceId;

    private GameRoom(String code, UUID gameDefinitionId, int versionNumber, UUID hostUserId) {
        this.code = code;
        this.gameDefinitionId = gameDefinitionId;
        this.versionNumber = versionNumber;
        this.hostUserId = hostUserId;
        this.createdAt = Instant.now();
    }

    public static GameRoom create(String code, UUID gameDefinitionId, int versionNumber, UUID hostUserId) {
        return new GameRoom(code, gameDefinitionId, versionNumber, hostUserId);
    }

    public synchronized RoomPlayer join(String displayName) {
        if (status != RoomStatus.OPEN) {
            throw new RoomNotOpenException(code);
        }
        RoomPlayer player = new RoomPlayer(UUID.randomUUID().toString(), displayName);
        players.add(player);
        return player;
    }

    public synchronized void start(UUID instanceId) {
        if (status != RoomStatus.OPEN) {
            throw new RoomNotOpenException(code);
        }
        if (players.isEmpty()) {
            throw new RoomEmptyException(code);
        }
        this.status = RoomStatus.STARTED;
        this.instanceId = instanceId;
    }

    public synchronized List<RoomPlayer> players() {
        return List.copyOf(players);
    }

    public synchronized RoomStatus status() {
        return status;
    }

    public synchronized UUID instanceId() {
        return instanceId;
    }

    public String code() {
        return code;
    }

    public UUID gameDefinitionId() {
        return gameDefinitionId;
    }

    public int versionNumber() {
        return versionNumber;
    }

    public UUID hostUserId() {
        return hostUserId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
