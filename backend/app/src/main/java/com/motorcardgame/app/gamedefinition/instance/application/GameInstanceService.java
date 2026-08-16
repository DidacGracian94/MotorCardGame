package com.motorcardgame.app.gamedefinition.instance.application;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstanceRepository;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.engine.config.GameSetupParser;
import com.motorcardgame.engine.config.GameStateSerializer;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.rule.Rule;
import com.motorcardgame.engine.rule.RuleEngine;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameInstanceService {

    private final GameInstanceRepository repository;
    private final GameDefinitionService gameDefinitionService;
    private final GameDefinitionVersionService gameDefinitionVersionService;
    private final RuleSetParser ruleSetParser;
    private final GameSetupParser gameSetupParser;
    private final GameStateSerializer gameStateSerializer;

    public GameInstanceService(
            GameInstanceRepository repository,
            GameDefinitionService gameDefinitionService,
            GameDefinitionVersionService gameDefinitionVersionService,
            RuleSetParser ruleSetParser,
            GameSetupParser gameSetupParser,
            GameStateSerializer gameStateSerializer) {
        this.repository = repository;
        this.gameDefinitionService = gameDefinitionService;
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.ruleSetParser = ruleSetParser;
        this.gameSetupParser = gameSetupParser;
        this.gameStateSerializer = gameStateSerializer;
    }

    /**
     * Construye el {@code GameState} inicial a partir del {@code config} de la versión (zonas +
     * mazo, ver {@link GameSetupParser}), dispara {@code GAME_STARTED} contra las reglas ya
     * parseadas de esa misma config — así el reparto inicial (si el config lo define) se expresa
     * con las mismas capacidades (REPEAT/ALL_PLAYERS/DRAW_CARDS) que cualquier otra regla, no con
     * una ruta especial — y persiste el resultado serializado.
     */
    @Transactional
    public GameInstance create(UUID gameDefinitionId, int versionNumber, List<Player> players) {
        GameDefinitionVersion version = gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, versionNumber);
        List<Rule> rules = ruleSetParser.parse(version.config());
        GameState state = gameSetupParser.buildInitialState(version.config(), players);
        new RuleEngine(rules).handle(Event.of("GAME_STARTED"), state);
        GameInstance instance = GameInstance.create(gameDefinitionId, version.id(), gameStateSerializer.toJson(state));
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
