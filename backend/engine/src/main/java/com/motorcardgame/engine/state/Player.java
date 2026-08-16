package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Un jugador dentro de una partida. Identidad por {@link PlayerId}.
 */
public final class Player {

    private final PlayerId id;
    private final String displayName;

    public Player(PlayerId id, String displayName) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public PlayerId id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Player player)) {
            return false;
        }
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
