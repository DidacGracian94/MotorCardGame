package com.motorcardgame.engine.state;

/**
 * Quién puede ver el contenido de una zona — independiente de {@link Ownership} (quién es "dueño"
 * de la zona no tiene por qué coincidir con quién puede verla; el ejemplo canónico es un juego
 * estilo Hanabi, donde tu propia mano es {@code PER_PLAYER} pero solo la ven los demás). Por
 * defecto (sin declarar en la config), una zona {@code SHARED} es {@code PUBLIC} y una
 * {@code PER_PLAYER} es {@code OWNER_ONLY} — el comportamiento de siempre, así que una config que
 * nunca menciona "visibility" sigue funcionando igual.
 */
public enum ZoneVisibility {
    /** Visible para cualquiera, incluidos los espectadores. */
    PUBLIC,
    /** Nadie ve el contenido — ni siquiera el dueño en una zona {@code PER_PLAYER} (mazo boca abajo). */
    HIDDEN,
    /** Solo el jugador dueño de la zona la ve (sentido habitual de "mano"). Requiere {@code PER_PLAYER}. */
    OWNER_ONLY,
    /** Todos la ven salvo su propio dueño (Hanabi). Requiere {@code PER_PLAYER}. */
    ALL_BUT_OWNER
}
