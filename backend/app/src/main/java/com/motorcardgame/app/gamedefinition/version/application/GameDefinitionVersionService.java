package com.motorcardgame.app.gamedefinition.version.application;

import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersionRepository;
import com.motorcardgame.engine.config.GameSetupParser;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
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

    /**
     * Jugador sintético usado solo para ejercitar la construcción del estado inicial durante la
     * validación de un config al publicar — nunca se persiste ni sale de este método. Con un único
     * jugador se recorren también las zonas PER_PLAYER (con cero jugadores ese camino no se
     * probaría en absoluto).
     */
    private static final List<Player> VALIDATION_PLAYERS = List.of(new Player(new PlayerId("validation"), "Validation"));

    private final GameDefinitionVersionRepository repository;
    private final GameDefinitionService gameDefinitionService;
    private final RuleSetParser ruleSetParser;
    private final GameSetupParser gameSetupParser;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public GameDefinitionVersionService(
            GameDefinitionVersionRepository repository,
            GameDefinitionService gameDefinitionService,
            RuleSetParser ruleSetParser,
            GameSetupParser gameSetupParser,
            PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.gameDefinitionService = gameDefinitionService;
        this.ruleSetParser = ruleSetParser;
        this.gameSetupParser = gameSetupParser;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public GameDefinitionVersion publish(
            UUID gameDefinitionId, String config, UUID actingUserId, Role actingUserRole) {
        gameDefinitionService.assertEditable(gameDefinitionId, actingUserId, actingUserRole);
        ruleSetParser.parse(config);
        ruleSetParser.validateEvents(config);
        gameSetupParser.buildInitialState(config, VALIDATION_PLAYERS);

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

    public List<GameDefinitionVersion> listByGameDefinition(
            UUID gameDefinitionId, UUID actingUserId, Role actingUserRole) {
        gameDefinitionService.assertVisible(gameDefinitionId, actingUserId, actingUserRole);
        return repository.findByGameDefinitionId(gameDefinitionId);
    }

    public GameDefinitionVersion getByVersionNumber(
            UUID gameDefinitionId, int versionNumber, UUID actingUserId, Role actingUserRole) {
        gameDefinitionService.assertVisible(gameDefinitionId, actingUserId, actingUserRole);
        return repository
                .findByGameDefinitionIdAndVersionNumber(gameDefinitionId, versionNumber)
                .orElseThrow(() -> new GameDefinitionVersionNotFoundException(gameDefinitionId, versionNumber));
    }

    public GameDefinitionVersion getById(UUID versionId) {
        return repository.findById(versionId)
                .orElseThrow(() -> new GameDefinitionVersionNotFoundException(versionId));
    }
}
