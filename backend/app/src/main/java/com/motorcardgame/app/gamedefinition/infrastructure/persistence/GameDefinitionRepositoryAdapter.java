package com.motorcardgame.app.gamedefinition.infrastructure.persistence;

import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class GameDefinitionRepositoryAdapter implements GameDefinitionRepository {

    private final SpringDataGameDefinitionRepository springDataRepository;

    GameDefinitionRepositoryAdapter(SpringDataGameDefinitionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public GameDefinition save(GameDefinition gameDefinition) {
        GameDefinitionJpaEntity saved = springDataRepository.save(toEntity(gameDefinition));
        return toDomain(saved);
    }

    @Override
    public Optional<GameDefinition> findById(UUID id) {
        return springDataRepository.findById(id).map(GameDefinitionRepositoryAdapter::toDomain);
    }

    @Override
    public List<GameDefinition> findAll() {
        return springDataRepository.findAll().stream()
                .map(GameDefinitionRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOwnerIdAndSlug(UUID ownerId, String slug) {
        return springDataRepository.existsByOwnerIdAndSlug(ownerId, slug);
    }

    private static GameDefinitionJpaEntity toEntity(GameDefinition domain) {
        return new GameDefinitionJpaEntity(
                domain.id(),
                domain.ownerId(),
                domain.name(),
                domain.slug(),
                domain.createdAt(),
                domain.updatedAt());
    }

    private static GameDefinition toDomain(GameDefinitionJpaEntity entity) {
        return GameDefinition.reconstitute(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getSlug(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
