package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardTest {

    @Test
    void withAttributeAddsNewAttributeKeepingOthers() {
        Card card = new Card(new CardId("c1"), Map.of("suit", "oros"));

        Card stamped = card.withAttribute("playedBy", "alice");

        assertEquals("oros", stamped.attribute("suit"));
        assertEquals("alice", stamped.attribute("playedBy"));
        assertNull(card.attribute("playedBy"));
    }

    @Test
    void withAttributeOverwritesExistingAttribute() {
        Card card = new Card(new CardId("c1"), Map.of("rank", 1));

        Card updated = card.withAttribute("rank", 2);

        assertEquals(2, updated.attribute("rank"));
    }

    @Test
    void withAttributeKeepsSameIdAndEquality() {
        Card card = new Card(new CardId("c1"), Map.of());

        Card stamped = card.withAttribute("playedBy", "alice");

        assertEquals(card, stamped);
        assertEquals(card.id(), stamped.id());
    }
}
