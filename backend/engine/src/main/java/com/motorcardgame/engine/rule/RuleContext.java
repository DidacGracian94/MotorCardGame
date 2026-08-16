package com.motorcardgame.engine.rule;

import com.motorcardgame.engine.event.Event;
import com.motorcardgame.engine.state.GameState;

import java.util.Objects;
import java.util.Optional;

/**
 * Contexto de ejecución de una regla: el {@link GameState} de la partida, el {@link Event} que
 * disparó la evaluación y, solo durante la ejecución de una {@link Action}, el target resuelto
 * sobre el que se está actuando. Es una lente inmutable sobre un {@link GameState} mutable — el
 * estado muta, el bundle de punteros no.
 */
public final class RuleContext {

    private final GameState gameState;
    private final Event event;
    private final Object target;

    public RuleContext(GameState gameState, Event event) {
        this(gameState, event, null);
    }

    private RuleContext(GameState gameState, Event event, Object target) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.event = Objects.requireNonNull(event, "event");
        this.target = target;
    }

    public GameState gameState() {
        return gameState;
    }

    public Event event() {
        return event;
    }

    public Optional<Object> target() {
        return Optional.ofNullable(target);
    }

    public RuleContext withTarget(Object target) {
        return new RuleContext(gameState, event, target);
    }
}
