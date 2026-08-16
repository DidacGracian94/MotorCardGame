package com.motorcardgame.app.gamedefinition.version.application;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GameDefinitionVersionService {

    private static final int MAX_PUBLISH_ATTEMPTS = 3;

    private final GameDefinitionVersionRepository repository;
    private final GameDefinitionService gameDefinitionService;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public GameDefinitionVersionService(
            GameDefinitionVersionRepository repository,
            GameDefinitionService gameDefinitionService,
            PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.gameDefinitionService = gameDefinitionService;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public GameDefinitionVersion publish(UUID gameDefinitionId, String config) {
        gameDefinitionService.getById(gameDefinitionId);

        for (int attempt = 1; attempt <= MAX_PUBLISH_ATTEMPTS; attempt++) {
            try {
                return publishAttempt(gameDefinitionId, config);
            } catch (DataIntegrityViolationException conflict) {
                if (attempt == MAX_PUBLISH_ATTEMPTS) {
                    throw new VersionPublishConflictException(gameDefinitionId, attempt);
                }
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private GameDefinitionVersion publishAttempt(UUID gameDefinitionId, String config) {
        return requiresNewTransactionTemplate.execute(status -> {
            int nextVersionNumber = repository.countByGameDefinitionId(gameDefinitionId) + 1;
            GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, nextVersionNumber, config);
            return repository.save(version);
        });
    }

    public List<GameDefinitionVersion> listByGameDefinition(UUID gameDefinitionId) {
        gameDefinitionService.getById(gameDefinitionId);
        return repository.findByGameDefinitionId(gameDefinitionId);
    }

    public GameDefinitionVersion getByVersionNumber(UUID gameDefinitionId, int versionNumber) {
        return repository
                .findByGameDefinitionIdAndVersionNumber(gameDefinitionId, versionNumber)
                .orElseThrow(() -> new GameDefinitionVersionNotFoundException(gameDefinitionId, versionNumber));
    }
}
