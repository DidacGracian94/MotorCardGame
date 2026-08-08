package com.motorcardgame.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EngineTest {

    @Test
    void reportsItsVersion() {
        assertEquals("0.1.0-SNAPSHOT", Engine.VERSION);
    }
}
