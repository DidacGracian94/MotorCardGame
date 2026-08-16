package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Identidad de una carta dentro de una partida.
 */
public record CardId(String value) {

    public CardId {
        Objects.requireNonNull(value, "value");
    }
}
