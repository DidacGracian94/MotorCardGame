package com.motorcardgame.app.gamedefinition.instance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.app.gamedefinition.instance.domain.GameInstance;
import com.motorcardgame.app.gamedefinition.instance.web.dto.GameInstanceResponse;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionService;
import com.motorcardgame.app.gamedefinition.version.domain.GameDefinitionVersion;
import com.motorcardgame.engine.config.GameSetupParser;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.state.ZoneVisibility;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Punto único donde se construye un {@link GameInstanceResponse} para un viewer concreto: resuelve
 * la versión de la instancia para conocer la visibilidad declarada de cada zona
 * ({@code zones[].visibility} en la config) y se la pasa a {@link GameStateVisibility}. Lo usan
 * tanto los controladores REST como {@code GameInstanceRealtimePublisher} — sin esto, cada sitio
 * que necesita responder con el estado de una instancia tendría que repetir "cargar la versión,
 * parsear la visibilidad de sus zonas".
 */
@Component
public class GameInstanceResponseFactory {

    private final GameDefinitionVersionService gameDefinitionVersionService;
    private final GameSetupParser gameSetupParser;
    private final RuleSetParser ruleSetParser;
    private final ObjectMapper objectMapper;

    public GameInstanceResponseFactory(
            GameDefinitionVersionService gameDefinitionVersionService,
            GameSetupParser gameSetupParser,
            RuleSetParser ruleSetParser,
            ObjectMapper objectMapper) {
        this.gameDefinitionVersionService = gameDefinitionVersionService;
        this.gameSetupParser = gameSetupParser;
        this.ruleSetParser = ruleSetParser;
        this.objectMapper = objectMapper;
    }

    public GameInstanceResponse forViewer(GameInstance instance, String viewerPlayerId)
            throws JsonProcessingException {
        GameDefinitionVersion version = gameDefinitionVersionService.getById(instance.gameDefinitionVersionId());
        Map<String, ZoneVisibility> zoneVisibility = gameSetupParser.parseZoneVisibility(version.config());
        return GameInstanceResponse.from(
                instance, objectMapper, viewerPlayerId, zoneVisibility, ruleSetParser.parsePlayerActions(version.config()));
    }
}
