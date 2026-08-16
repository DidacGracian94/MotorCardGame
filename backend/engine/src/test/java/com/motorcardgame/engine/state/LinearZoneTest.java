package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinearZoneTest {

    @Test
    void popsTopInLifoOrder() {
        LinearZone zone = new LinearZone();
        Card first = new Card(new CardId("c1"), Map.of());
        Card second = new Card(new CardId("c2"), Map.of());

        zone.pushTop(first);
        zone.pushTop(second);

        assertEquals(second, zone.popTop());
        assertEquals(first, zone.popTop());
    }

    @Test
    void pushBottomAndPopBottom() {
        LinearZone zone = new LinearZone();
        Card first = new Card(new CardId("c1"), Map.of());
        Card second = new Card(new CardId("c2"), Map.of());

        zone.pushBottom(first);
        zone.pushBottom(second);

        assertEquals(second, zone.popBottom());
        assertEquals(first, zone.popBottom());
    }

    @Test
    void popTopOnEmptyZoneThrows() {
        LinearZone zone = new LinearZone();

        assertThrows(NoSuchElementException.class, zone::popTop);
    }

    @Test
    void peekTopAndPeekBottomDoNotRemoveCards() {
        LinearZone zone = new LinearZone();
        Card first = new Card(new CardId("c1"), Map.of());
        Card second = new Card(new CardId("c2"), Map.of());

        zone.pushTop(first);
        zone.pushTop(second);

        assertEquals(second, zone.peekTop());
        assertEquals(first, zone.peekBottom());
        assertEquals(2, zone.size());
    }

    @Test
    void peekBottomOnEmptyZoneThrows() {
        LinearZone zone = new LinearZone();

        assertThrows(NoSuchElementException.class, zone::peekBottom);
    }

    @Test
    void cardsViewReflectsTopToBottomOrder() {
        LinearZone zone = new LinearZone();
        Card first = new Card(new CardId("c1"), Map.of());
        Card second = new Card(new CardId("c2"), Map.of());

        zone.pushTop(first);
        zone.pushTop(second);

        assertEquals(List.of(second, first), zone.cardsView());
    }

    @Test
    void ofTopToBottomPreservesGivenOrder() {
        Card first = new Card(new CardId("c1"), Map.of());
        Card second = new Card(new CardId("c2"), Map.of());
        Card third = new Card(new CardId("c3"), Map.of());

        LinearZone zone = LinearZone.ofTopToBottom(List.of(first, second, third));

        assertEquals(List.of(first, second, third), zone.cardsView());
        assertEquals(first, zone.peekTop());
        assertEquals(third, zone.peekBottom());
    }

    @Test
    void ofTopToBottomOfEmptyListIsEmptyZone() {
        LinearZone zone = LinearZone.ofTopToBottom(List.of());

        assertTrue(zone.isEmpty());
    }

    @Test
    void sizeAndIsEmpty() {
        LinearZone zone = new LinearZone();

        assertTrue(zone.isEmpty());
        assertEquals(0, zone.size());

        zone.pushTop(new Card(new CardId("c1"), Map.of()));

        assertEquals(1, zone.size());
        assertTrue(!zone.isEmpty());
    }
}
