package com.motorcardgame.engine.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Zona con topología de pila lineal: solo importan el orden, el tope y el fondo (mazos, manos,
 * pilas de descarte). Cubre todo lo que necesita el roadmap actual (UNO/Brisca/Escoba).
 */
public final class LinearZone implements Zone {

    private final Deque<Card> cards = new ArrayDeque<>();

    /**
     * Construye una zona con las cartas dadas en el orden indicado (primer elemento = tope). Lo
     * usan tanto la construcción del estado inicial desde config (mazo ya barajado) como la
     * reconstrucción desde el JSON persistido de un {@link GameState}, donde el orden exacto debe
     * preservarse.
     */
    public static LinearZone ofTopToBottom(List<Card> cardsTopToBottom) {
        LinearZone zone = new LinearZone();
        for (int i = cardsTopToBottom.size() - 1; i >= 0; i--) {
            zone.pushTop(cardsTopToBottom.get(i));
        }
        return zone;
    }

    public void pushTop(Card card) {
        cards.addFirst(Objects.requireNonNull(card, "card"));
    }

    public Card popTop() {
        Card card = cards.pollFirst();
        if (card == null) {
            throw new NoSuchElementException("Zone is empty");
        }
        return card;
    }

    public Card peekTop() {
        Card card = cards.peekFirst();
        if (card == null) {
            throw new NoSuchElementException("Zone is empty");
        }
        return card;
    }

    public Card peekBottom() {
        Card card = cards.peekLast();
        if (card == null) {
            throw new NoSuchElementException("Zone is empty");
        }
        return card;
    }

    public void pushBottom(Card card) {
        cards.addLast(Objects.requireNonNull(card, "card"));
    }

    public Card popBottom() {
        Card card = cards.pollLast();
        if (card == null) {
            throw new NoSuchElementException("Zone is empty");
        }
        return card;
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> cardsView() {
        return List.copyOf(cards);
    }
}
