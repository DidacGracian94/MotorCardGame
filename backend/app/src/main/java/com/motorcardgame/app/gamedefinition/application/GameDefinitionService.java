package com.motorcardgame.app.gamedefinition.application;

import com.motorcardgame.app.auth.domain.Role;
import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionRepository;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionVisibility;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameDefinitionService {

    private final GameDefinitionRepository gameDefinitionRepository;

    public GameDefinitionService(GameDefinitionRepository gameDefinitionRepository) {
        this.gameDefinitionRepository = gameDefinitionRepository;
    }

    @Transactional
    public GameDefinition create(UUID ownerId, String name, String slug) {
        if (gameDefinitionRepository.existsByOwnerIdAndSlug(ownerId, slug)) {
            throw new SlugAlreadyExistsException(ownerId, slug);
        }
        GameDefinition gameDefinition = GameDefinition.create(ownerId, name, slug);
        return gameDefinitionRepository.save(gameDefinition);
    }

    @Transactional(readOnly = true)
    public GameDefinition getById(UUID id) {
        return gameDefinitionRepository.findById(id)
                .orElseThrow(() -> new GameDefinitionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<GameDefinition> listAll() {
        return gameDefinitionRepository.findAll();
    }

    /** Admin ve todas; un usuario normal ve las suyas más las PUBLIC de cualquiera. */
    @Transactional(readOnly = true)
    public List<GameDefinition> listVisibleTo(UUID actingUserId, Role actingUserRole) {
        if (actingUserRole == Role.ADMIN) {
            return gameDefinitionRepository.findAll();
        }
        return gameDefinitionRepository.findVisibleTo(actingUserId);
    }

    /** Admin, dueño, o PUBLIC — si no, {@link GameDefinitionAccessDeniedException}. */
    @Transactional(readOnly = true)
    public GameDefinition assertVisible(UUID id, UUID actingUserId, Role actingUserRole) {
        GameDefinition definition = getById(id);
        if (!definition.isVisibleTo(actingUserId, actingUserRole == Role.ADMIN)) {
            throw new GameDefinitionAccessDeniedException(id);
        }
        return definition;
    }

    /** Admin o dueño únicamente — si no, {@link GameDefinitionAccessDeniedException}. */
    @Transactional(readOnly = true)
    public GameDefinition assertEditable(UUID id, UUID actingUserId, Role actingUserRole) {
        GameDefinition definition = getById(id);
        if (!definition.isEditableBy(actingUserId, actingUserRole == Role.ADMIN)) {
            throw new GameDefinitionAccessDeniedException(id);
        }
        return definition;
    }

    @Transactional
    public GameDefinition rename(UUID id, String newName, UUID actingUserId, Role actingUserRole) {
        GameDefinition gameDefinition = assertEditable(id, actingUserId, actingUserRole);
        gameDefinition.rename(newName);
        return gameDefinitionRepository.save(gameDefinition);
    }

    @Transactional
    public GameDefinition changeVisibility(
            UUID id, GameDefinitionVisibility newVisibility, UUID actingUserId, Role actingUserRole) {
        GameDefinition gameDefinition = assertEditable(id, actingUserId, actingUserRole);
        gameDefinition.changeVisibility(newVisibility);
        return gameDefinitionRepository.save(gameDefinition);
    }
}
