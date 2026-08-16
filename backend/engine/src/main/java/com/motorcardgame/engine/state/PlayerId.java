package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Identidad de un jugador dentro de una partida.
 */
public record PlayerId(String value) {

    public PlayerId {
        Objects.requireNonNull(value, "value");
    }
}
