package com.motorcardgame.app.gamedefinition.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameDefinitionRepository {

    GameDefinition save(GameDefinition gameDefinition);

    Optional<GameDefinition> findById(UUID id);

    List<GameDefinition> findAll();

    List<GameDefinition> findVisibleTo(UUID ownerId);

    boolean existsByOwnerIdAndSlug(UUID ownerId, String slug);
}
