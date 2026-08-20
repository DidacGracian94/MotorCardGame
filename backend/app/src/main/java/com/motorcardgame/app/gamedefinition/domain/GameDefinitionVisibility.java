package com.motorcardgame.app.gamedefinition.domain;

/**
 * Visibilidad de una {@code GameDefinition} para otros usuarios (no confundir con
 * {@code GameStateVisibility}, que es la visibilidad de una zona dentro de una partida en curso —
 * un concepto no relacionado).
 */
public enum GameDefinitionVisibility {
    PRIVATE,
    PUBLIC
}
