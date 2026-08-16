package com.motorcardgame.app.gamedefinition.instance.application;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstanceRepository;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.engine.config.RuleSetParser;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameInstanceService {

    /**
     * El motor todavía no sabe construir un {@code GameState} real desde {@code config} (falta
     * parsing de mazos/zonas/jugadores) ni serializarlo a JSON — ese trabajo llega junto con
     * {@code PlayerAction}. Hasta entonces, toda instancia arranca con este placeholder.
     */
    private static final String INITIAL_STATE = "{\"status\":\"NOT_STARTED\"}";

    private final GameInstanceRepository repository;
    private final GameDefinitionService gameDefinitionService;
    private final GameDefinitionVersionService gameDefinitionVersionService;
    private final RuleSetParser ruleSetParser;

    public GameInstanceService(
            GameInstanceRepository repository,
            GameDefinitionService gameDefinitionService,
            GameDefinitionVersionService gameDefinitionVersionService,
            RuleSetParser ruleSetParser) {
        this.repository = repository;
        this.gameDefinitionService = gameDefinitionService;
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.ruleSetParser = ruleSetParser;
    }

    @Transactional
    public GameInstance create(UUID gameDefinitionId, int versionNumber) {
        GameDefinitionVersion version = gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, versionNumber);
        ruleSetParser.parse(version.config());
        GameInstance instance = GameInstance.create(gameDefinitionId, version.id(), INITIAL_STATE);
        return repository.save(instance);
    }

    @Transactional(readOnly = true)
    public GameInstance getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new GameInstanceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<GameInstance> listByGameDefinition(UUID gameDefinitionId) {
        gameDefinitionService.getById(gameDefinitionId);
        return repository.findByGameDefinitionId(gameDefinitionId);
    }
}
