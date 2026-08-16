package com.motorcardgame.engine.state;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameStateTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

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
}
