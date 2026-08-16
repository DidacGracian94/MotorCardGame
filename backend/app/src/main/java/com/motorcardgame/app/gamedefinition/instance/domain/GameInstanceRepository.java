package com.motorcardgame.app.gamedefinition.instance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameInstanceRepository {

    GameInstance save(GameInstance instance);

    Optional<GameInstance> findById(UUID id);

    List<GameInstance> findByGameDefinitionId(UUID gameDefinitionId);
}
