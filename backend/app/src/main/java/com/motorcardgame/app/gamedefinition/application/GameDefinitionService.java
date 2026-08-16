package com.motorcardgame.app.gamedefinition.application;

import com.motorcardgame.app.gamedefinition.domain.GameDefinition;
import com.motorcardgame.app.gamedefinition.domain.GameDefinitionRepository;
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

    @Transactional
    public GameDefinition rename(UUID id, String newName) {
        GameDefinition gameDefinition = getById(id);
        gameDefinition.rename(newName);
        return gameDefinitionRepository.save(gameDefinition);
    }
}
