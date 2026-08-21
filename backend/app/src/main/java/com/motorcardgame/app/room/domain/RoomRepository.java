package com.motorcardgame.app.room.domain;

import java.util.Optional;

public interface RoomRepository {

    void save(GameRoom room);

    Optional<GameRoom> findByCode(String code);

    boolean existsByCode(String code);
}
