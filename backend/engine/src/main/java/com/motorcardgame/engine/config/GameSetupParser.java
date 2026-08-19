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
import com.motorcardgame.engine.state.ZoneVisibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    /**
     * Lee, por cada zona declarada, quién puede ver su contenido — independiente de a quién
     * pertenece (ver {@link ZoneVisibility}). No participa en la construcción del {@link GameState}
     * (la visibilidad no es una regla de juego, es un dato de cara a serializar la respuesta para
     * un viewer concreto), así que vive aparte de {@link #buildInitialState}.
     */
    public Map<String, ZoneVisibility> parseZoneVisibility(String configJson) {
        JsonNode root;
        try {
            root = objectMapper.readTree(configJson);
        } catch (JsonProcessingException e) {
            throw new InvalidGameDefinitionException("config is not valid JSON: " + e.getOriginalMessage(), e);
        }

        Map<String, ZoneVisibility> result = new HashMap<>();
        for (JsonNode zoneNode : JsonNodes.requiredArray(root, "zones")) {
            ZoneRef ref = ZoneRefs.fromJson(zoneNode);
            result.put(ref.name(), parseVisibility(zoneNode, ref.ownership()));
        }
        return result;
    }

    private static ZoneVisibility parseVisibility(JsonNode zoneNode, Ownership ownership) {
        ZoneVisibility defaultVisibility =
                ownership == Ownership.SHARED ? ZoneVisibility.PUBLIC : ZoneVisibility.OWNER_ONLY;
        String text = JsonNodes.optionalText(zoneNode, "visibility", defaultVisibility.name());

        ZoneVisibility visibility;
        try {
            visibility = ZoneVisibility.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new InvalidGameDefinitionException("unknown zone visibility \"" + text + "\": " + zoneNode, e);
        }

        boolean ownerScoped = visibility == ZoneVisibility.OWNER_ONLY || visibility == ZoneVisibility.ALL_BUT_OWNER;
        if (ownerScoped && ownership != Ownership.PER_PLAYER) {
            throw new InvalidGameDefinitionException(
                    "zone visibility \"" + visibility + "\" requires ownership PER_PLAYER: " + zoneNode);
        }
        return visibility;
    }
}
