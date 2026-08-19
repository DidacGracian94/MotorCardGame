package com.motorcardgame.engine.state;

import java.util.Objects;

/**
 * Identidad de una carta dentro de una partida.
 */
public record CardId(String value) {

    public CardId {
        Objects.requireNonNull(value, "value");
    }

    /**
     * Id de la plantilla ({@code cards[].id} en la config) que originó esta carta, deshaciendo el
     * sufijo {@code "#<n>"} que {@code CardCatalog} añade siempre al generar instancias (incluso
     * con {@code count == 1}). Se corta en el primer {@code '#'} — por eso {@code CardCatalog}
     * rechaza plantillas cuyo id ya contenga uno, para que este corte nunca sea ambiguo.
     */
    public String templateId() {
        int separator = value.indexOf('#');
        return separator < 0 ? value : value.substring(0, separator);
    }
}
