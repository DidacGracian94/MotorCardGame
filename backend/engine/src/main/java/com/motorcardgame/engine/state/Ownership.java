package com.motorcardgame.engine.state;

/**
 * A qué método distinto de {@link GameState} debe direccionarse una zona: {@code SHARED} (mazo o
 * pila compartida entre todos los jugadores) o {@code PER_PLAYER} (cada jugador tiene su propia
 * instancia de la zona). Ver la nota sobre el modelo de Zone en CLAUDE.md.
 */
public enum Ownership {
    SHARED,
    PER_PLAYER
}
