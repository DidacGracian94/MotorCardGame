package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");
    private static final Player CAROL = new Player(new PlayerId("carol"), "Carol");

    @Test
    void currentPlayerIsFirstPlayerInitially() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertEquals(ALICE, state.currentPlayer());
    }

    @Test
    void advanceTurnWrapsAroundAfterLastPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));

        state.advanceTurn();
        assertEquals(BOB, state.currentPlayer());

        state.advanceTurn();
        assertEquals(ALICE, state.currentPlayer());
    }

    @Test
    void registersAndReturnsSharedZone() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();

        state.registerSharedZone("pile", pile);

        assertSame(pile, state.sharedZone("pile"));
    }

    @Test
    void rejectsDuplicateSharedZoneName() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("pile", new LinearZone());

        assertThrows(IllegalStateException.class,
                () -> state.registerSharedZone("pile", new LinearZone()));
    }

    @Test
    void throwsOnUnknownSharedZone() {
        GameState state = new GameState(List.of(ALICE));

        assertThrows(NoSuchElementException.class, () -> state.sharedZone("unknown"));
    }

    @Test
    void playerZonesAreIsolatedPerPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone aliceHand = new LinearZone();
        LinearZone bobHand = new LinearZone();

        state.registerPlayerZone(ALICE.id(), "hand", aliceHand);
        state.registerPlayerZone(BOB.id(), "hand", bobHand);

        assertSame(aliceHand, state.zoneOf(ALICE.id(), "hand"));
        assertSame(bobHand, state.zoneOf(BOB.id(), "hand"));
    }

    @Test
    void zoneOfCurrentPlayerDelegatesToCurrentPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone aliceHand = new LinearZone();
        state.registerPlayerZone(ALICE.id(), "hand", aliceHand);

        assertSame(aliceHand, state.zoneOfCurrentPlayer("hand"));
    }

    @Test
    void directionDefaultsToForward() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertEquals(1, state.direction());
    }

    @Test
    void reverseDirectionMakesAdvanceTurnGoBackwards() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));

        state.reverseDirection();
        state.advanceTurn();

        assertEquals(CAROL, state.currentPlayer());
        assertEquals(-1, state.direction());
    }

    @Test
    void nextPlayerDoesNotMutateCurrentPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));

        assertEquals(BOB, state.nextPlayer());
        assertEquals(ALICE, state.currentPlayer());
    }

    @Test
    void nextPlayerRespectsReversedDirection() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));

        state.reverseDirection();

        assertEquals(CAROL, state.nextPlayer());
    }

    @Test
    void constructorWithDirectionRestoresReversedState() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL), 0, -1);

        assertEquals(-1, state.direction());
        assertEquals(CAROL, state.nextPlayer());
    }

    @Test
    void constructorRejectsInvalidDirection() {
        assertThrows(IllegalArgumentException.class, () -> new GameState(List.of(ALICE, BOB), 0, 0));
    }

    @Test
    void constructorWithIndexStartsAtGivenPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB), 1);

        assertEquals(BOB, state.currentPlayer());
        assertEquals(1, state.currentPlayerIndex());
    }

    @Test
    void constructorRejectsOutOfRangeIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> new GameState(List.of(ALICE, BOB), 2));
        assertThrows(IndexOutOfBoundsException.class, () -> new GameState(List.of(ALICE, BOB), -1));
    }

    @Test
    void sharedZonesReturnsAllRegisteredSharedZones() {
        GameState state = new GameState(List.of(ALICE));
        LinearZone pile = new LinearZone();
        state.registerSharedZone("pile", pile);

        assertEquals(Map.of("pile", pile), state.sharedZones());
    }

    @Test
    void perPlayerZonesReturnsAllRegisteredPlayerZones() {
        GameState state = new GameState(List.of(ALICE, BOB));
        LinearZone aliceHand = new LinearZone();
        LinearZone bobHand = new LinearZone();
        state.registerPlayerZone(ALICE.id(), "hand", aliceHand);
        state.registerPlayerZone(BOB.id(), "hand", bobHand);

        assertEquals(Map.of("hand", Map.of(ALICE.id(), aliceHand, BOB.id(), bobHand)), state.perPlayerZones());
    }

    @Test
    void isCurrentPlayerTrueForCurrentPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertTrue(state.isCurrentPlayer(ALICE.id()));
    }

    @Test
    void isCurrentPlayerFalseForOtherPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertFalse(state.isCurrentPlayer(BOB.id()));
    }

    @Test
    void isCurrentPlayerFalseForUnknownPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertFalse(state.isCurrentPlayer(new PlayerId("carol")));
    }

    @Test
    void setCurrentPlayerJumpsDirectlyToGivenPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB, CAROL));

        state.setCurrentPlayer(CAROL.id());

        assertEquals(CAROL, state.currentPlayer());
        assertEquals(2, state.currentPlayerIndex());
    }

    @Test
    void setCurrentPlayerThrowsForUnknownPlayer() {
        GameState state = new GameState(List.of(ALICE, BOB));

        assertThrows(NoSuchElementException.class, () -> state.setCurrentPlayer(new PlayerId("carol")));
    }

    @Test
    void variableIsNullBeforeItIsEverSet() {
        GameState state = new GameState(List.of(ALICE));

        assertNull(state.variable("trumpSuit"));
    }

    @Test
    void variableRoundTripsAfterBeingSet() {
        GameState state = new GameState(List.of(ALICE));

        state.setVariable("trumpSuit", "oros");

        assertEquals("oros", state.variable("trumpSuit"));
    }

    @Test
    void setVariableRejectsNullValue() {
        GameState state = new GameState(List.of(ALICE));

        assertThrows(NullPointerException.class, () -> state.setVariable("trumpSuit", null));
    }

    @Test
    void variablesViewDoesNotExposeMutationOfUnderlyingState() {
        GameState state = new GameState(List.of(ALICE));
        state.setVariable("trumpSuit", "oros");

        assertTrue(state.variables().containsKey("trumpSuit"));
        assertThrows(UnsupportedOperationException.class,
                () -> state.variables().put("ledSuit", "copas"));
    }

    @Test
    void sharedZonesViewDoesNotExposeMutationOfUnderlyingState() {
        GameState state = new GameState(List.of(ALICE));
        state.registerSharedZone("pile", new LinearZone());

        assertTrue(state.sharedZones().containsKey("pile"));
        assertThrows(UnsupportedOperationException.class,
                () -> state.sharedZones().put("discard", new LinearZone()));
    }
}
