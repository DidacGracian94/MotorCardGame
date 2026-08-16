package com.motorcardgame.engine.config;

import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.PlayerId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSetupParserTest {

    private static final Player ALICE = new Player(new PlayerId("alice"), "Alice");
    private static final Player BOB = new Player(new PlayerId("bob"), "Bob");

    @Test
    void registersSharedZonePopulatedFromCatalog() {
        GameSetupParser parser = new GameSetupParser();
        String config = """
                {
                  "zones": [ { "name": "deck", "ownership": "SHARED" } ],
                  "cards": [ { "id": "red-1", "zone": "deck", "count": 3, "attributes": { "color": "RED" } } ]
                }
                """;

        GameState state = parser.buildInitialState(config, List.of(ALICE));

        assertEquals(3, ((LinearZone) state.sharedZone("deck")).size());
    }

    @Test
    void registersEmptyPerPlayerZoneForEachPlayer() {
        GameSetupParser parser = new GameSetupParser();
        String config = """
                { "zones": [ { "name": "hand", "ownership": "PER_PLAYER" } ] }
                """;

        GameState state = parser.buildInitialState(config, List.of(ALICE, BOB));

        assertTrue(((LinearZone) state.zoneOf(ALICE.id(), "hand")).isEmpty());
        assertTrue(((LinearZone) state.zoneOf(BOB.id(), "hand")).isEmpty());
    }

    @Test
    void shuffleIsDeterministicForAGivenRandomSeed() {
        String config = """
                {
                  "zones": [ { "name": "deck", "ownership": "SHARED", "shuffle": true } ],
                  "cards": [ { "id": "c", "zone": "deck", "count": 20, "attributes": {} } ]
                }
                """;

        GameState first = new GameSetupParser(new Random(42)).buildInitialState(config, List.of(ALICE));
        GameState second = new GameSetupParser(new Random(42)).buildInitialState(config, List.of(ALICE));

        assertEquals(
                ((LinearZone) first.sharedZone("deck")).cardsView(),
                ((LinearZone) second.sharedZone("deck")).cardsView());
    }

    @Test
    void withoutShuffleKeepsCatalogOrder() {
        GameSetupParser parser = new GameSetupParser();
        String config = """
                {
                  "zones": [ { "name": "deck", "ownership": "SHARED" } ],
                  "cards": [ { "id": "a", "zone": "deck", "attributes": {} }, { "id": "b", "zone": "deck", "attributes": {} } ]
                }
                """;

        GameState state = parser.buildInitialState(config, List.of(ALICE));

        assertEquals("a#1", ((LinearZone) state.sharedZone("deck")).peekTop().id().value());
        assertEquals("b#1", ((LinearZone) state.sharedZone("deck")).peekBottom().id().value());
    }

    @Test
    void cardsReferencingUndeclaredZoneAreInvalid() {
        GameSetupParser parser = new GameSetupParser();
        String config = """
                {
                  "zones": [ { "name": "deck", "ownership": "SHARED" } ],
                  "cards": [ { "id": "a", "zone": "discard", "attributes": {} } ]
                }
                """;

        assertThrows(InvalidGameDefinitionException.class, () -> parser.buildInitialState(config, List.of(ALICE)));
    }

    @Test
    void cardsReferencingPerPlayerZoneAreInvalid() {
        GameSetupParser parser = new GameSetupParser();
        String config = """
                {
                  "zones": [ { "name": "hand", "ownership": "PER_PLAYER" } ],
                  "cards": [ { "id": "a", "zone": "hand", "attributes": {} } ]
                }
                """;

        assertThrows(InvalidGameDefinitionException.class, () -> parser.buildInitialState(config, List.of(ALICE)));
    }

    @Test
    void missingZonesArrayIsInvalid() {
        GameSetupParser parser = new GameSetupParser();

        assertThrows(InvalidGameDefinitionException.class, () -> parser.buildInitialState("{}", List.of(ALICE)));
    }

    @Test
    void malformedJsonIsInvalid() {
        GameSetupParser parser = new GameSetupParser();

        assertThrows(InvalidGameDefinitionException.class, () -> parser.buildInitialState("{not json", List.of(ALICE)));
    }
}
