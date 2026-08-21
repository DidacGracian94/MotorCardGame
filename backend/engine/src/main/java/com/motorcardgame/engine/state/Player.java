package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Un jugador dentro de una partida. Identidad por {@link PlayerId}.
 */
public final class Player {

    private final PlayerId id;
    private final String displayName;
    private int score;

    public Player(PlayerId id, String displayName) {
        this(id, displayName, 0);
    }

    /**
     * Reconstruye un jugador con su puntuación ya acumulada — lo usa
     * {@code GameStateSerializer} al deserializar un {@link GameState} persistido, donde la
     * puntuación es parte de lo guardado, no algo que se recalcule.
     */
    public Player(PlayerId id, String displayName, int score) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.score = score;
    }

    public PlayerId id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int score() {
        return score;
    }

    /**
     * Suma puntos a este jugador — dispara la acción {@code ADD_POINTS} (p.ej. al llevarse las
     * cartas de una baza). Un delta negativo no tiene sentido para "puntos ganados", así que se
     * rechaza en vez de restar silenciosamente.
     */
    public void addScore(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must not be negative: " + delta);
        }
        score += delta;
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
