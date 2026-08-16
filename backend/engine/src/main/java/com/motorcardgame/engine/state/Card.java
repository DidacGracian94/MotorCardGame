package com.motorcardgame.engine.state;

import java.util.Map;
import java.util.Objects;

/**
 * Una carta dentro de una partida. Los atributos (color, número, palo, ...) son opacos al engine
 * — los define la configuración de cada juego, nunca el motor.
 */
public final class Card {

    private final CardId id;
    private final Map<String, Object> attributes;

    public Card(CardId id, Map<String, Object> attributes) {
        this.id = Objects.requireNonNull(id, "id");
        this.attributes = Map.copyOf(Objects.requireNonNull(attributes, "attributes"));
    }

    public CardId id() {
        return id;
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card card)) {
            return false;
        }
        return id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
