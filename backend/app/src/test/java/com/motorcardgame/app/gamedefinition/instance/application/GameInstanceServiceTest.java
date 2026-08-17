package com.motorcardgame.app.gamedefinition.instance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionNotFoundException;
import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstanceRepository;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionNotFoundException;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.engine.config.GameSetupParser;
import com.motorcardgame.engine.config.GameStateSerializer;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.rule.Rule;
import com.motorcardgame.engine.rule.condition.AndCondition;
import com.motorcardgame.engine.rule.action.NextPlayerAction;
import com.motorcardgame.engine.rule.target.CurrentPlayerTarget;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameInstanceServiceTest {

    private static final List<Player> PLAYERS = List.of(new Player(new PlayerId("alice"), "Alice"));

    @Mock
    private GameInstanceRepository repository;

    @Mock
    private GameDefinitionService gameDefinitionService;

    @Mock
    private GameDefinitionVersionService gameDefinitionVersionService;

    @Mock
    private RuleSetParser ruleSetParser;

    @Mock
    private GameSetupParser gameSetupParser;

    @Mock
    private GameStateSerializer gameStateSerializer;

    private GameInstanceService service;

    @BeforeEach
    void setUp() {
        service = new GameInstanceService(
                repository, gameDefinitionService, gameDefinitionVersionService,
                ruleSetParser, gameSetupParser, gameStateSerializer);
    }

    @Test
    void create_savesInstanceWithSerializedState_whenVersionExistsAndConfigIsValid() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{\"rules\":[],\"zones\":[]}");
        GameState state = new GameState(PLAYERS);
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1)).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of());
        when(gameSetupParser.buildInitialState(version.config(), PLAYERS)).thenReturn(state);
        when(gameStateSerializer.toJson(state)).thenReturn("{\"players\":[]}");
        when(repository.save(any(GameInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameInstance created = service.create(gameDefinitionId, 1, PLAYERS);

        assertThat(created.gameDefinitionId()).isEqualTo(gameDefinitionId);
        assertThat(created.gameDefinitionVersionId()).isEqualTo(version.id());
        assertThat(created.state()).isEqualTo("{\"players\":[]}");
        assertThat(created.endedAt()).isNull();
    }

    @Test
    void create_throwsVersionNotFound_whenVersionMissing() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1))
                .thenThrow(new GameDefinitionVersionNotFoundException(gameDefinitionId, 1));

        assertThatThrownBy(() -> service.create(gameDefinitionId, 1, PLAYERS))
                .isInstanceOf(GameDefinitionVersionNotFoundException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void create_propagatesInvalidGameDefinition_whenRulesAreInvalid() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{}");
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1)).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenThrow(new InvalidGameDefinitionException("boom"));

        assertThatThrownBy(() -> service.create(gameDefinitionId, 1, PLAYERS))
                .isInstanceOf(InvalidGameDefinitionException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void create_propagatesInvalidGameDefinition_whenZonesAreInvalid() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{\"rules\":[]}");
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1)).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of());
        when(gameSetupParser.buildInitialState(version.config(), PLAYERS))
                .thenThrow(new InvalidGameDefinitionException("boom"));

        assertThatThrownBy(() -> service.create(gameDefinitionId, 1, PLAYERS))
                .isInstanceOf(InvalidGameDefinitionException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void getById_returnsInstance_whenExists() {
        GameInstance existing = GameInstance.create(UUID.randomUUID(), UUID.randomUUID(), "{}");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));

        assertThat(service.getById(existing.id())).isEqualTo(existing);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(missingId))
                .isInstanceOf(GameInstanceNotFoundException.class);
    }

    @Test
    void listByGameDefinition_returnsInstances_whenDefinitionExists() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameInstance instance = GameInstance.create(gameDefinitionId, UUID.randomUUID(), "{}");
        when(gameDefinitionService.getById(gameDefinitionId))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.findByGameDefinitionId(gameDefinitionId)).thenReturn(List.of(instance));

        assertThat(service.listByGameDefinition(gameDefinitionId)).containsExactly(instance);
    }

    @Test
    void listByGameDefinition_throwsNotFound_whenDefinitionMissing() {
        UUID missingId = UUID.randomUUID();
        when(gameDefinitionService.getById(missingId)).thenThrow(new GameDefinitionNotFoundException(missingId));

        assertThatThrownBy(() -> service.listByGameDefinition(missingId))
                .isInstanceOf(GameDefinitionNotFoundException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void applyAction_savesUpdatedState_whenActingPlayerIsCurrentPlayer() {
        PlayerId alice = new PlayerId("alice");
        PlayerId bob = new PlayerId("bob");
        GameState state = new GameState(List.of(new Player(alice, "Alice"), new Player(bob, "Bob")));
        GameInstance instance = GameInstance.create(UUID.randomUUID(), UUID.randomUUID(), "{\"before\":true}");
        GameDefinitionVersion version = GameDefinitionVersion.publish(instance.gameDefinitionId(), 1, "{\"rules\":[]}");
        Rule advanceTurnRule = new Rule(
                "PLAYER_REQUESTED_NEXT_TURN", new AndCondition(), new CurrentPlayerTarget(), new NextPlayerAction());
        when(repository.findById(instance.id())).thenReturn(Optional.of(instance));
        when(gameDefinitionVersionService.getById(instance.gameDefinitionVersionId())).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of(advanceTurnRule));
        when(gameStateSerializer.fromJson(instance.state())).thenReturn(state);
        when(gameStateSerializer.toJson(state)).thenReturn("{\"after\":true}");
        when(repository.save(any(GameInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameInstance updated = service.applyAction(instance.id(), alice, "PLAYER_REQUESTED_NEXT_TURN", Map.of());

        assertThat(updated.state()).isEqualTo("{\"after\":true}");
        assertThat(state.currentPlayer().id()).isEqualTo(bob);
    }

    @Test
    void applyAction_throwsNotPlayersTurn_whenActingPlayerIsNotCurrentPlayer() {
        PlayerId alice = new PlayerId("alice");
        PlayerId bob = new PlayerId("bob");
        GameState state = new GameState(List.of(new Player(alice, "Alice"), new Player(bob, "Bob")));
        GameInstance instance = GameInstance.create(UUID.randomUUID(), UUID.randomUUID(), "{\"before\":true}");
        GameDefinitionVersion version = GameDefinitionVersion.publish(instance.gameDefinitionId(), 1, "{\"rules\":[]}");
        when(repository.findById(instance.id())).thenReturn(Optional.of(instance));
        when(gameDefinitionVersionService.getById(instance.gameDefinitionVersionId())).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of());
        when(gameStateSerializer.fromJson(instance.state())).thenReturn(state);

        assertThatThrownBy(() -> service.applyAction(instance.id(), bob, "PLAYER_REQUESTED_NEXT_TURN", Map.of()))
                .isInstanceOf(NotPlayersTurnException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void applyAction_throwsPlayerActionRejected_whenNoRuleMatchesEvent() {
        PlayerId alice = new PlayerId("alice");
        GameState state = new GameState(List.of(new Player(alice, "Alice")));
        GameInstance instance = GameInstance.create(UUID.randomUUID(), UUID.randomUUID(), "{\"before\":true}");
        GameDefinitionVersion version = GameDefinitionVersion.publish(instance.gameDefinitionId(), 1, "{\"rules\":[]}");
        when(repository.findById(instance.id())).thenReturn(Optional.of(instance));
        when(gameDefinitionVersionService.getById(instance.gameDefinitionVersionId())).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of());
        when(gameStateSerializer.fromJson(instance.state())).thenReturn(state);

        assertThatThrownBy(() -> service.applyAction(instance.id(), alice, "CARD_PLAYED", Map.of()))
                .isInstanceOf(PlayerActionRejectedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void applyAction_throwsInstanceNotFound_whenInstanceMissing() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyAction(missingId, new PlayerId("alice"), "ANY_EVENT", Map.of()))
                .isInstanceOf(GameInstanceNotFoundException.class);
        verifyNoInteractions(gameDefinitionVersionService);
    }

    @Test
    void applyAction_throwsVersionNotFound_whenVersionMissing() {
        GameInstance instance = GameInstance.create(UUID.randomUUID(), UUID.randomUUID(), "{}");
        when(repository.findById(instance.id())).thenReturn(Optional.of(instance));
        when(gameDefinitionVersionService.getById(instance.gameDefinitionVersionId()))
                .thenThrow(new GameDefinitionVersionNotFoundException(instance.gameDefinitionVersionId()));

        assertThatThrownBy(() -> service.applyAction(instance.id(), new PlayerId("alice"), "ANY_EVENT", Map.of()))
                .isInstanceOf(GameDefinitionVersionNotFoundException.class);
        verify(repository, never()).save(any());
    }
}
