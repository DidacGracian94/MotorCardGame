package com.motorcardgame.app.room.infrastructure;

import com.motorcardgame.app.room.domain.GameRoom;
import com.motorcardgame.app.room.domain.RoomRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Sin tabla ni migración: las salas son sesiones cortas y desechables (ver {@link GameRoom}), así
 * que un mapa en memoria del propio proceso basta y evita el coste de modelarlas en Postgres.
 */
@Repository
class InMemoryRoomRepository implements RoomRepository {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(GameRoom room) {
        rooms.put(room.code(), room);
    }

    @Override
    public Optional<GameRoom> findByCode(String code) {
        return Optional.ofNullable(rooms.get(code));
    }

    @Override
    public boolean existsByCode(String code) {
        return rooms.containsKey(code);
    }
}
