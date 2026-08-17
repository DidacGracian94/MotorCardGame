package com.motorcardgame.app.gamedefinition.version.infrastructure.persistence;

import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class GameDefinitionVersionRepositoryAdapter implements GameDefinitionVersionRepository {

    private final SpringDataGameDefinitionVersionRepository springDataRepository;

    GameDefinitionVersionRepositoryAdapter(SpringDataGameDefinitionVersionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public GameDefinitionVersion save(GameDefinitionVersion version) {
        GameDefinitionVersionJpaEntity saved = springDataRepository.save(toEntity(version));
        return toDomain(saved);
    }

    @Override
    public List<GameDefinitionVersion> findByGameDefinitionId(UUID gameDefinitionId) {
        return springDataRepository.findByGameDefinitionId(gameDefinitionId).stream()
                .map(GameDefinitionVersionRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<GameDefinitionVersion> findById(UUID id) {
        return springDataRepository.findById(id).map(GameDefinitionVersionRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<GameDefinitionVersion> findByGameDefinitionIdAndVersionNumber(
            UUID gameDefinitionId, int versionNumber) {
        return springDataRepository
                .findByGameDefinitionIdAndVersionNumber(gameDefinitionId, versionNumber)
                .map(GameDefinitionVersionRepositoryAdapter::toDomain);
    }

    @Override
    public int countByGameDefinitionId(UUID gameDefinitionId) {
        return (int) springDataRepository.countByGameDefinitionId(gameDefinitionId);
    }

    private static GameDefinitionVersionJpaEntity toEntity(GameDefinitionVersion domain) {
        return new GameDefinitionVersionJpaEntity(
                domain.id(),
                domain.gameDefinitionId(),
                domain.versionNumber(),
                domain.config(),
                domain.createdAt(),
                domain.publishedAt());
    }

    private static GameDefinitionVersion toDomain(GameDefinitionVersionJpaEntity entity) {
        return GameDefinitionVersion.reconstitute(
                entity.getId(),
                entity.getGameDefinitionId(),
                entity.getVersionNumber(),
                entity.getConfig(),
                entity.getCreatedAt(),
                entity.getPublishedAt());
    }
}
