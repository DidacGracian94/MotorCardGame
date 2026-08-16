package com.motorcardgame.engine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import com.motorcardgame.engine.state.Card;
import com.motorcardgame.engine.state.GameState;
import com.motorcardgame.engine.state.LinearZone;
import com.motorcardgame.engine.state.Ownership;
import com.motorcardgame.engine.state.Player;
import com.motorcardgame.engine.state.ZoneRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Construye el {@link GameState} inicial de una partida a partir del {@code config} de una
 * {@code GameDefinitionVersion} y la lista de jugadores: registra las zonas declaradas en
 * {@code "zones"} y las puebla con las cartas de {@code "cards"} (solo zonas {@code SHARED} — a
 * las zonas {@code PER_PLAYER} se reparte con reglas, no con el catálogo, reutilizando
 * {@code REPEAT}/{@code ALL_PLAYERS}/{@code DRAW_CARDS} en vez de una segunda forma de mover
 * cartas).
 */
public final class GameSetupParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random;

    public GameSetupParser() {
        this(new Random());
    }

    public GameSetupParser(Random random) {
        this.random = random;
    }

    public GameState buildInitialState(String configJson, List<Player> players) {
        JsonNode root;
        try {
            root = objectMapper.readTree(configJson);
        } catch (JsonProcessingException e) {
            throw new InvalidGameDefinitionException("config is not valid JSON: " + e.getOriginalMessage(), e);
        }

        Map<String, List<Card>> cardsByZone = CardCatalog.parseByZone(root.path("cards"));
        Set<String> sharedZoneNames = new HashSet<>();
        GameState state = new GameState(players);

        for (JsonNode zoneNode : JsonNodes.requiredArray(root, "zones")) {
            ZoneRef ref = ZoneRefs.fromJson(zoneNode);
            if (ref.ownership() == Ownership.SHARED) {
                sharedZoneNames.add(ref.name());
                List<Card> initialCards = new ArrayList<>(cardsByZone.getOrDefault(ref.name(), List.of()));
                if (zoneNode.path("shuffle").asBoolean(false)) {
                    Collections.shuffle(initialCards, random);
                }
                state.registerSharedZone(ref.name(), LinearZone.ofTopToBottom(initialCards));
            } else {
                for (Player player : players) {
                    state.registerPlayerZone(player.id(), ref.name(), new LinearZone());
                }
            }
        }

        for (String zoneName : cardsByZone.keySet()) {
            if (!sharedZoneNames.contains(zoneName)) {
                throw new InvalidGameDefinitionException(
                        "\"cards\" references a zone that is not a declared SHARED zone: " + zoneName);
            }
        }

        return state;
    }
}
