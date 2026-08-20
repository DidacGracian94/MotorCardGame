package com.motorcardgame.app.gamedefinition.infrastructure.persistence;

import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGameDefinitionRepository extends JpaRepository<GameDefinitionJpaEntity, UUID> {

    boolean existsByOwnerIdAndSlug(UUID ownerId, String slug);

    List<GameDefinitionJpaEntity> findByOwnerIdOrVisibility(UUID ownerId, GameDefinitionVisibility visibility);
}
