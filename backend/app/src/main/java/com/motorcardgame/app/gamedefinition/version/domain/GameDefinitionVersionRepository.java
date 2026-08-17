package com.motorcardgame.app.gamedefinition.version.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameDefinitionVersionRepository {

    GameDefinitionVersion save(GameDefinitionVersion version);

    List<GameDefinitionVersion> findByGameDefinitionId(UUID gameDefinitionId);

    Optional<GameDefinitionVersion> findById(UUID id);

    Optional<GameDefinitionVersion> findByGameDefinitionIdAndVersionNumber(UUID gameDefinitionId, int versionNumber);

    int countByGameDefinitionId(UUID gameDefinitionId);
}
