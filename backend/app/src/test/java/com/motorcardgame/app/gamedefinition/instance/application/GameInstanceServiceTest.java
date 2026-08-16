package com.motorcardgame.app.gamedefinition.instance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameInstanceServiceTest {

    @Mock
    private GameInstanceRepository repository;

    @Mock
    private GameDefinitionService gameDefinitionService;

    @Mock
    private GameDefinitionVersionService gameDefinitionVersionService;

    @Mock
    private RuleSetParser ruleSetParser;

    private GameInstanceService service;

    @BeforeEach
    void setUp() {
        service = new GameInstanceService(repository, gameDefinitionService, gameDefinitionVersionService, ruleSetParser);
    }

    @Test
    void create_savesInstanceWithPlaceholderState_whenVersionExistsAndConfigIsValid() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{\"rules\":[]}");
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1)).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenReturn(List.of());
        when(repository.save(any(GameInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameInstance created = service.create(gameDefinitionId, 1);

        assertThat(created.gameDefinitionId()).isEqualTo(gameDefinitionId);
        assertThat(created.gameDefinitionVersionId()).isEqualTo(version.id());
        assertThat(created.state()).isEqualTo("{\"status\":\"NOT_STARTED\"}");
        assertThat(created.endedAt()).isNull();
    }

    @Test
    void create_throwsVersionNotFound_whenVersionMissing() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1))
                .thenThrow(new GameDefinitionVersionNotFoundException(gameDefinitionId, 1));

        assertThatThrownBy(() -> service.create(gameDefinitionId, 1))
                .isInstanceOf(GameDefinitionVersionNotFoundException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void create_propagatesInvalidGameDefinition_whenConfigIsInvalid() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{}");
        when(gameDefinitionVersionService.getByVersionNumber(gameDefinitionId, 1)).thenReturn(version);
        when(ruleSetParser.parse(version.config())).thenThrow(new InvalidGameDefinitionException("boom"));

        assertThatThrownBy(() -> service.create(gameDefinitionId, 1))
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
}
