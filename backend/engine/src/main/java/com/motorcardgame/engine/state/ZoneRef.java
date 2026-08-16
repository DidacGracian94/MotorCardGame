package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Referencia a una zona por nombre y ownership, tal y como la describe la configuración de un
 * juego. Es el punto donde ese dato (que solo puede venir de fuera del motor, p.ej. de JSON)
 * decide a cuál de los dos métodos distintos de {@link GameState} llamar —
 * {@link GameState#sharedZone(String)} o {@link GameState#zoneOf(PlayerId, String)} —
 * preservando la distinción por compilación que {@link GameState} ya hace en su propia API.
 */
public record ZoneRef(String name, Ownership ownership) {

    public ZoneRef {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ownership, "ownership");
    }

    public Zone resolve(GameState state, Player contextPlayer) {
        Objects.requireNonNull(state, "state");
        if (ownership == Ownership.SHARED) {
            return state.sharedZone(name);
        }
        Objects.requireNonNull(contextPlayer, "contextPlayer");
        return state.zoneOf(contextPlayer.id(), name);
    }
}
