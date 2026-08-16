package com.motorcardgame.app.gamedefinition.instance.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGameInstanceRepository extends JpaRepository<GameInstanceJpaEntity, UUID> {

    List<GameInstanceJpaEntity> findByGameDefinitionId(UUID gameDefinitionId);
}
