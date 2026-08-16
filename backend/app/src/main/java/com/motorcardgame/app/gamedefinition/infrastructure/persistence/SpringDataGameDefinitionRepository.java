package com.motorcardgame.app.gamedefinition.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGameDefinitionRepository extends JpaRepository<GameDefinitionJpaEntity, UUID> {

    boolean existsByOwnerIdAndSlug(UUID ownerId, String slug);
}
