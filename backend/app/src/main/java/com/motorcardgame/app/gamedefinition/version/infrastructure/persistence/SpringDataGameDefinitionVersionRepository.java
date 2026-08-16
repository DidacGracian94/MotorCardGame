package com.motorcardgame.app.gamedefinition.version.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGameDefinitionVersionRepository extends JpaRepository<GameDefinitionVersionJpaEntity, UUID> {

    List<GameDefinitionVersionJpaEntity> findByGameDefinitionId(UUID gameDefinitionId);

    Optional<GameDefinitionVersionJpaEntity> findByGameDefinitionIdAndVersionNumber(
            UUID gameDefinitionId, int versionNumber);

    long countByGameDefinitionId(UUID gameDefinitionId);
}
