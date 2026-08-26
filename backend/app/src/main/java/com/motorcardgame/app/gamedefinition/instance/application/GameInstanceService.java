package com.motorcardgame.app.gamedefinition.instance.application;

import com.motorcardgame.app.auth.domain.Role;
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
import com.motorcardgame.engine.state.PlayerId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public GameInstanceService(
            GameInstanceRepository repository,
            GameDefinitionService gameDefinitionService,
            GameDefinitionVersionService gameDefinitionVersionService,
            RuleSetParser ruleSetParser,
            GameSetupParser gameSetupParser,
            GameStateSerializer gameStateSerializer,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.gameDefinitionService = gameDefinitionService;
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.ruleSetParser = ruleSetParser;
        this.gameSetupParser = gameSetupParser;
        this.gameStateSerializer = gameStateSerializer;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Construye el {@code GameState} inicial a partir del {@code config} de la versión (zonas +
     * mazo, ver {@link GameSetupParser}), dispara {@code GAME_STARTED} contra las reglas ya
     * parseadas de esa misma config — así el reparto inicial (si el config lo define) se expresa
     * con las mismas capacidades (REPEAT/ALL_PLAYERS/DRAW_CARDS) que cualquier otra regla, no con
     * una ruta especial — y persiste el resultado serializado.
     */
    @Transactional
    public GameInstance create(
            UUID gameDefinitionId,
            int versionNumber,
            List<Player> players,
            UUID actingUserId,
            Role actingUserRole) {
        GameDefinitionVersion version = gameDefinitionVersionService.getByVersionNumber(
                gameDefinitionId, versionNumber, actingUserId, actingUserRole);
        List<Rule> rules = ruleSetParser.parse(version.config());
        GameState state = gameSetupParser.buildInitialState(version.config(), players);
        new RuleEngine(rules).handle(Event.of("GAME_STARTED"), state);
        GameInstance instance = GameInstance.create(gameDefinitionId, version.id(), gameStateSerializer.toJson(state));
        GameInstance saved = repository.save(instance);
        eventPublisher.publishEvent(new GameInstanceUpdatedEvent(saved.id()));
        return saved;
    }

    @Transactional(readOnly = true)
    public GameInstance getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new GameInstanceNotFoundException(id));
    }

    /**
     * Valida que {@code eventType} esté declarado en {@code playerActions} de esa versión (un
     * jugador no puede disparar una acción que el diseñador del juego no ha preparado), carga el
     * estado persistido de la instancia, valida que quien actúa es el jugador con el turno actual,
     * despacha el evento contra las reglas de la versión con la que se creó la instancia, y
     * persiste el resultado. No conoce ningún tipo de evento concreto más allá de eso — cualquier
     * capacidad que la config de esa versión ya tenga cableada (DRAW_CARDS, NEXT_PLAYER, ...) es
     * la que decide qué ocurre.
     */
    @Transactional
    public GameInstance applyAction(UUID instanceId, PlayerId actingPlayerId, String eventType, Map<String, Object> payload) {
        GameInstance instance = getById(instanceId);
        if (instance.endedAt() != null) {
            throw new GameAlreadyEndedException(instanceId);
        }
        GameDefinitionVersion version = gameDefinitionVersionService.getById(instance.gameDefinitionVersionId());
        if (!ruleSetParser.parsePlayerActions(version.config()).contains(eventType)) {
            throw new UnknownPlayerActionException(instanceId, eventType);
        }
        List<Rule> rules = ruleSetParser.parse(version.config());
        GameState state = gameStateSerializer.fromJson(instance.state());

        if (!state.isCurrentPlayer(actingPlayerId)) {
            throw new NotPlayersTurnException(instanceId, actingPlayerId, state.currentPlayer().id());
        }

        boolean matched = new RuleEngine(rules).handle(new Event(eventType, payload), state);
        if (!matched) {
            throw new PlayerActionRejectedException(instanceId, actingPlayerId, eventType);
        }

        GameInstance updated = instance.withState(gameStateSerializer.toJson(state));
        if (state.isEnded()) {
            updated = updated.withEndedAt(Instant.now());
        }
        GameInstance saved = repository.save(updated);
        eventPublisher.publishEvent(new GameInstanceUpdatedEvent(saved.id()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<GameInstance> listByGameDefinition(UUID gameDefinitionId, UUID actingUserId, Role actingUserRole) {
        gameDefinitionService.assertVisible(gameDefinitionId, actingUserId, actingUserRole);
        return repository.findByGameDefinitionId(gameDefinitionId);
    }
}
