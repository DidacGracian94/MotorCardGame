package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardIdTest {

    @Test
    void templateIdCutsAtTheGeneratedInstanceSuffix() {
        assertEquals("reverso-rojo", new CardId("reverso-rojo#1").templateId());
        assertEquals("wild", new CardId("wild#12").templateId());
    }

    @Test
    void templateIdReturnsTheWholeValueWhenThereIsNoSuffix() {
        assertEquals("no-suffix", new CardId("no-suffix").templateId());
    }
}
