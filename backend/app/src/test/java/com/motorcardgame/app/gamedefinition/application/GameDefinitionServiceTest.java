package com.motorcardgame.app.gamedefinition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameDefinitionServiceTest {

    @Mock
    private GameDefinitionRepository repository;

    private GameDefinitionService service;

    @BeforeEach
    void setUp() {
        service = new GameDefinitionService(repository);
    }

    @Test
    void create_savesNewGameDefinition_whenSlugIsUnique() {
        UUID ownerId = UUID.randomUUID();
        when(repository.existsByOwnerIdAndSlug(ownerId, "mi-juego")).thenReturn(false);
        when(repository.save(any(GameDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinition created = service.create(ownerId, "Mi Juego", "mi-juego");

        assertThat(created.ownerId()).isEqualTo(ownerId);
        assertThat(created.name()).isEqualTo("Mi Juego");
        assertThat(created.slug()).isEqualTo("mi-juego");

        ArgumentCaptor<GameDefinition> captor = ArgumentCaptor.forClass(GameDefinition.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(created.id());
    }

    @Test
    void create_throwsSlugAlreadyExists_whenSlugTaken() {
        UUID ownerId = UUID.randomUUID();
        when(repository.existsByOwnerIdAndSlug(ownerId, "mi-juego")).thenReturn(true);

        assertThatThrownBy(() -> service.create(ownerId, "Mi Juego", "mi-juego"))
                .isInstanceOf(SlugAlreadyExistsException.class);
    }

    @Test
    void getById_returnsGameDefinition_whenExists() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));

        GameDefinition found = service.getById(existing.id());

        assertThat(found).isEqualTo(existing);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(missingId))
                .isInstanceOf(GameDefinitionNotFoundException.class);
    }

    @Test
    void rename_updatesNameAndPersists() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Nombre Viejo", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(repository.save(any(GameDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinition renamed = service.rename(existing.id(), "Nombre Nuevo");

        assertThat(renamed.name()).isEqualTo("Nombre Nuevo");
        verify(repository).save(existing);
    }
}
