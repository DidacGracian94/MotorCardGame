package com.motorcardgame.engine.config;

import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.CardId;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateSerializerTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    private final GameStateSerializer serializer = new GameStateSerializer();

    @Test
    void roundTripsPlayersTurnAndZones() {
        GameState original = new GameState(List.of(ALICE, BOB), 1);
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c2"), Map.of("color", "BLUE")));
        pile.pushTop(new Card(new CardId("c1"), Map.of("color", "RED", "value", 7)));
        original.registerSharedZone("pile", pile);
        original.registerPlayerZone(ALICE.id(), "hand", new LinearZone());
        LinearZone bobHand = new LinearZone();
        bobHand.pushTop(new Card(new CardId("c3"), Map.of()));
        original.registerPlayerZone(BOB.id(), "hand", bobHand);

        GameState restored = serializer.fromJson(serializer.toJson(original));

        assertEquals(original.players(), restored.players());
        assertEquals(BOB, restored.currentPlayer());
        assertEquals(1, restored.currentPlayerIndex());
        assertEquals(
                ((LinearZone) original.sharedZone("pile")).cardsView(),
                ((LinearZone) restored.sharedZone("pile")).cardsView());
        assertEquals(
                ((LinearZone) original.zoneOf(BOB.id(), "hand")).cardsView(),
                ((LinearZone) restored.zoneOf(BOB.id(), "hand")).cardsView());
        assertTrue(((LinearZone) restored.zoneOf(ALICE.id(), "hand")).isEmpty());
    }

    @Test
    void preservesCardAttributeTypes() {
        GameState original = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        pile.pushTop(new Card(new CardId("c1"), Map.of("color", "RED", "value", 7, "wild", false)));
        original.registerSharedZone("pile", pile);

        GameState restored = serializer.fromJson(serializer.toJson(original));

        Card restoredCard = ((LinearZone) restored.sharedZone("pile")).peekTop();
        assertEquals("RED", restoredCard.attribute("color"));
        assertEquals(7, restoredCard.attribute("value"));
        assertEquals(false, restoredCard.attribute("wild"));
    }

    @Test
    void roundTripsReversedDirection() {
        GameState original = new GameState(List.of(ALICE, BOB));
        original.reverseDirection();

        GameState restored = serializer.fromJson(serializer.toJson(original));

        assertEquals(-1, restored.direction());
    }

    @Test
    void defaultsToForwardDirectionWhenFieldIsAbsent() {
        String legacyStateWithoutDirection = """
                {
                  "players": [{ "id": "alice", "displayName": "Alice" }],
                  "currentPlayerIndex": 0,
                  "sharedZones": {},
                  "perPlayerZones": {}
                }
                """;

        GameState restored = serializer.fromJson(legacyStateWithoutDirection);

        assertEquals(1, restored.direction());
    }

    @Test
    void malformedJsonIsInvalid() {
        assertThrows(InvalidGameDefinitionException.class, () -> serializer.fromJson("{not json"));
    }

    @Test
    void roundTripsPlayerScore() {
        GameState original = new GameState(List.of(ALICE, BOB));
        original.players().get(0).addScore(11);
        original.players().get(1).addScore(4);

        GameState restored = serializer.fromJson(serializer.toJson(original));

        assertEquals(11, restored.players().get(0).score());
        assertEquals(4, restored.players().get(1).score());
    }

    @Test
    void defaultsToZeroScoreWhenFieldIsAbsent() {
        String legacyStateWithoutScore = """
                {
                  "players": [{ "id": "alice", "displayName": "Alice" }],
                  "currentPlayerIndex": 0,
                  "sharedZones": {},
                  "perPlayerZones": {}
                }
                """;

        GameState restored = serializer.fromJson(legacyStateWithoutScore);

        assertEquals(0, restored.currentPlayer().score());
    }

    @Test
    void roundTripsVariables() {
        GameState original = new GameState(List.of(ALICE));
        original.setVariable("trumpSuit", "oros");
        original.setVariable("dealtCount", 6);

        GameState restored = serializer.fromJson(serializer.toJson(original));

        assertEquals("oros", restored.variable("trumpSuit"));
        assertEquals(6, restored.variable("dealtCount"));
    }

    @Test
    void defaultsToNoVariablesWhenFieldIsAbsent() {
        String legacyStateWithoutVariables = """
                {
                  "players": [{ "id": "alice", "displayName": "Alice" }],
                  "currentPlayerIndex": 0,
                  "sharedZones": {},
                  "perPlayerZones": {}
                }
                """;

        GameState restored = serializer.fromJson(legacyStateWithoutVariables);

        assertNull(restored.variable("trumpSuit"));
    }
}
