package com.motorcardgame.app.gamedefinition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionRepository;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
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
        assertThat(created.visibility()).isEqualTo(GameDefinitionVisibility.PRIVATE);

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
    void rename_updatesNameAndPersists_whenActingUserIsOwner() {
        UUID ownerId = UUID.randomUUID();
        GameDefinition existing = GameDefinition.create(ownerId, "Nombre Viejo", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(repository.save(any(GameDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinition renamed = service.rename(existing.id(), "Nombre Nuevo", ownerId, Role.USER);

        assertThat(renamed.name()).isEqualTo("Nombre Nuevo");
        verify(repository).save(existing);
    }

    @Test
    void rename_updatesNameAndPersists_whenActingUserIsAdmin() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Nombre Viejo", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(repository.save(any(GameDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameDefinition renamed = service.rename(existing.id(), "Nombre Nuevo", UUID.randomUUID(), Role.ADMIN);

        assertThat(renamed.name()).isEqualTo("Nombre Nuevo");
    }

    @Test
    void rename_throwsAccessDenied_whenActingUserIsNeitherOwnerNorAdmin() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Nombre Viejo", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.rename(existing.id(), "Nombre Nuevo", UUID.randomUUID(), Role.USER))
                .isInstanceOf(GameDefinitionAccessDeniedException.class);
    }

    @Test
    void assertVisible_throwsAccessDenied_whenPrivateAndNotOwnerNorAdmin() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego");
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.assertVisible(existing.id(), UUID.randomUUID(), Role.USER))
                .isInstanceOf(GameDefinitionAccessDeniedException.class);
    }

    @Test
    void assertVisible_returnsDefinition_whenPublicAndNotOwner() {
        GameDefinition existing = GameDefinition.create(UUID.randomUUID(), "Mi Juego", "mi-juego");
        existing.changeVisibility(GameDefinitionVisibility.PUBLIC);
        when(repository.findById(existing.id())).thenReturn(Optional.of(existing));

        GameDefinition visible = service.assertVisible(existing.id(), UUID.randomUUID(), Role.USER);

        assertThat(visible).isEqualTo(existing);
    }

    @Test
    void listVisibleTo_returnsFindAll_whenAdmin() {
        UUID adminId = UUID.randomUUID();
        service.listVisibleTo(adminId, Role.ADMIN);
        verify(repository).findAll();
    }

    @Test
    void listVisibleTo_returnsFindVisibleTo_whenNormalUser() {
        UUID userId = UUID.randomUUID();
        service.listVisibleTo(userId, Role.USER);
        verify(repository).findVisibleTo(userId);
    }
}
