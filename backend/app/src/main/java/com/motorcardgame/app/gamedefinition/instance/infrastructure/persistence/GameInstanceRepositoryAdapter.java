package com.motorcardgame.app.gamedefinition.instance.infrastructure.persistence;

import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstanceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class GameInstanceRepositoryAdapter implements GameInstanceRepository {

    private final SpringDataGameInstanceRepository springDataRepository;

    GameInstanceRepositoryAdapter(SpringDataGameInstanceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public GameInstance save(GameInstance instance) {
        GameInstanceJpaEntity saved = springDataRepository.save(toEntity(instance));
        return toDomain(saved);
    }

    @Override
    public Optional<GameInstance> findById(UUID id) {
        return springDataRepository.findById(id).map(GameInstanceRepositoryAdapter::toDomain);
    }

    @Override
    public List<GameInstance> findByGameDefinitionId(UUID gameDefinitionId) {
        return springDataRepository.findByGameDefinitionId(gameDefinitionId).stream()
                .map(GameInstanceRepositoryAdapter::toDomain)
                .toList();
    }

    private static GameInstanceJpaEntity toEntity(GameInstance domain) {
        return new GameInstanceJpaEntity(
                domain.id(),
                domain.gameDefinitionId(),
                domain.gameDefinitionVersionId(),
                domain.state(),
                domain.createdAt(),
                domain.endedAt());
    }

    private static GameInstance toDomain(GameInstanceJpaEntity entity) {
        return GameInstance.reconstitute(
                entity.getId(),
                entity.getGameDefinitionId(),
                entity.getGameDefinitionVersionId(),
                entity.getState(),
                entity.getCreatedAt(),
                entity.getEndedAt());
    }
}
