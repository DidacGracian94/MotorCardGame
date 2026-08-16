package com.motorcardgame.app.config;

import com.motorcardgame.engine.config.GameSetupParser;
import com.motorcardgame.engine.config.GameStateSerializer;
import com.motorcardgame.engine.config.RuleSetParser;
import com.motorcardgame.engine.rule.registry.ActionRegistry;
import com.motorcardgame.engine.rule.registry.ConditionRegistry;
import com.motorcardgame.engine.rule.registry.StandardCapabilities;
import com.motorcardgame.engine.rule.registry.TargetRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto de entrada al motor desde {@code app}: registries poblados con las capacidades estándar
 * del motor, envueltas en un {@link RuleSetParser} listo para convertir el {@code config} de una
 * {@code GameDefinitionVersion} en {@code Rule} reales, más los beans para construir y persistir
 * el {@code GameState} inicial de una partida.
 */
@Configuration
class EngineConfig {

    @Bean
    RuleSetParser ruleSetParser() {
        ActionRegistry actionRegistry = new ActionRegistry();
        ConditionRegistry conditionRegistry = new ConditionRegistry();
        TargetRegistry targetRegistry = new TargetRegistry();
        StandardCapabilities.registerInto(actionRegistry, conditionRegistry, targetRegistry);
        return new RuleSetParser(actionRegistry, conditionRegistry, targetRegistry);
    }

    @Bean
    GameSetupParser gameSetupParser() {
        return new GameSetupParser();
    }

    @Bean
    GameStateSerializer gameStateSerializer() {
        return new GameStateSerializer();
    }
}
