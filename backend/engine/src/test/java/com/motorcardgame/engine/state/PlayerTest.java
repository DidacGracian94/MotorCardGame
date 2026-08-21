package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    @Test
    void scoreDefaultsToZero() {
        Player player = new Player(new PlayerId("alice"), "Alice");

        assertEquals(0, player.score());
    }

    @Test
    void addScoreAccumulates() {
        Player player = new Player(new PlayerId("alice"), "Alice");

        player.addScore(11);
        player.addScore(4);

        assertEquals(15, player.score());
    }

    @Test
    void addScoreRejectsNegativeDelta() {
        Player player = new Player(new PlayerId("alice"), "Alice");

        assertThrows(IllegalArgumentException.class, () -> player.addScore(-1));
    }

    @Test
    void reconstructionConstructorRestoresScore() {
        Player player = new Player(new PlayerId("alice"), "Alice", 15);

        assertEquals(15, player.score());
    }

    @Test
    void equalityIsStillByIdOnlyRegardlessOfScore() {
        Player fresh = new Player(new PlayerId("alice"), "Alice");
        Player withScore = new Player(new PlayerId("alice"), "Alice", 15);

        assertTrue(fresh.equals(withScore));
        assertEquals(fresh.hashCode(), withScore.hashCode());
    }
}
