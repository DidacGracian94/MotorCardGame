package com.motorcardgame.app.gamedefinition.version.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.application.GameDefinitionNotFoundException;
import com.motorcardgame.app.gamedefinition.application.GameDefinitionService;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersionRepository;
import com.motorcardgame.engine.config.GameSetupParser;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class GameDefinitionVersionServiceTest {

    private static final UUID ACTING_USER_ID = UUID.randomUUID();
    private static final Role ACTING_USER_ROLE = Role.USER;

    @Mock
    private GameDefinitionVersionRepository repository;

    @Mock
    private GameDefinitionService gameDefinitionService;

    @Mock
    private RuleSetParser ruleSetParser;

    @Mock
    private GameSetupParser gameSetupParser;

    private GameDefinitionVersionService service;

    @BeforeEach
    void setUp() {
        service = new GameDefinitionVersionService(
                repository, gameDefinitionService, ruleSetParser, gameSetupParser, new NoOpTransactionManager());
    }

    @Test
    void publish_usesVersionNumberOne_whenNoPriorVersions() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.countByGameDefinitionId(gameDefinitionId)).thenReturn(0);
        when(repository.save(any(GameDefinitionVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinitionVersion published = service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE);

        assertThat(published.versionNumber()).isEqualTo(1);
        assertThat(published.gameDefinitionId()).isEqualTo(gameDefinitionId);
    }

    @Test
    void publish_incrementsVersionNumber_basedOnExistingCount() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.countByGameDefinitionId(gameDefinitionId)).thenReturn(2);
        when(repository.save(any(GameDefinitionVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinitionVersion published = service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE);

        assertThat(published.versionNumber()).isEqualTo(3);
    }

    @Test
    void publish_throwsGameDefinitionNotFound_whenParentMissing() {
        UUID missingId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(missingId), any(), any()))
                .thenThrow(new GameDefinitionNotFoundException(missingId));

        assertThatThrownBy(() -> service.publish(missingId, "{}", ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(GameDefinitionNotFoundException.class);
    }

    @Test
    void publish_retriesAndSucceeds_afterConflictOnEarlierAttempts() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.countByGameDefinitionId(gameDefinitionId)).thenReturn(0);
        doThrow(new DataIntegrityViolationException("conflicto"))
                .doThrow(new DataIntegrityViolationException("conflicto"))
                .doAnswer(invocation -> invocation.getArgument(0))
                .when(repository)
                .save(any(GameDefinitionVersion.class));

        GameDefinitionVersion published = service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE);

        assertThat(published).isNotNull();
        verify(repository, times(3)).save(any(GameDefinitionVersion.class));
    }

    @Test
    void publish_throwsVersionPublishConflict_afterExhaustingAllAttempts() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.countByGameDefinitionId(gameDefinitionId)).thenReturn(0);
        doThrow(new DataIntegrityViolationException("conflicto"))
                .when(repository)
                .save(any(GameDefinitionVersion.class));

        assertThatThrownBy(() -> service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(VersionPublishConflictException.class);
        verify(repository, times(3)).save(any(GameDefinitionVersion.class));
    }

    @Test
    void publish_throwsInvalidGameDefinition_whenRulesAreInvalid() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(ruleSetParser.parse("{}")).thenThrow(new InvalidGameDefinitionException("boom"));

        assertThatThrownBy(() -> service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(InvalidGameDefinitionException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void publish_throwsInvalidGameDefinition_whenRuleEventsAreNotDeclared() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        doThrow(new InvalidGameDefinitionException("boom")).when(ruleSetParser).validateEvents("{}");

        assertThatThrownBy(() -> service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(InvalidGameDefinitionException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void publish_throwsInvalidGameDefinition_whenZonesAreInvalid() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(gameDefinitionService.assertEditable(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(gameSetupParser.buildInitialState(eq("{}"), any()))
                .thenThrow(new InvalidGameDefinitionException("boom"));

        assertThatThrownBy(() -> service.publish(gameDefinitionId, "{}", ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(InvalidGameDefinitionException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void listByGameDefinition_returnsVersions_whenParentVisible() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{}");
        when(gameDefinitionService.assertVisible(eq(gameDefinitionId), any(), any()))
                .thenReturn(GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego"));
        when(repository.findByGameDefinitionId(gameDefinitionId)).thenReturn(List.of(version));

        assertThat(service.listByGameDefinition(gameDefinitionId, ACTING_USER_ID, ACTING_USER_ROLE))
                .containsExactly(version);
    }

    @Test
    void getByVersionNumber_throwsNotFound_whenMissing() {
        UUID gameDefinitionId = UUID.randomUUID();
        when(repository.findByGameDefinitionIdAndVersionNumber(gameDefinitionId, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByVersionNumber(gameDefinitionId, 1, ACTING_USER_ID, ACTING_USER_ROLE))
                .isInstanceOf(GameDefinitionVersionNotFoundException.class);
    }

    @Test
    void getById_returnsVersion_whenExists() {
        UUID gameDefinitionId = UUID.randomUUID();
        GameDefinitionVersion version = GameDefinitionVersion.publish(gameDefinitionId, 1, "{}");
        when(repository.findById(version.id())).thenReturn(Optional.of(version));

        assertThat(service.getById(version.id())).isEqualTo(version);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        UUID versionId = UUID.randomUUID();
        when(repository.findById(versionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(versionId))
                .isInstanceOf(GameDefinitionVersionNotFoundException.class);
    }

    /**
     * Doble de prueba mínimo: permite que {@link org.springframework.transaction.support.TransactionTemplate}
     * ejecute el callback sin una transacción ni base de datos reales por debajo.
     */
    private static final class NoOpTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {
            // no-op
        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
            // no-op
        }
    }
}
