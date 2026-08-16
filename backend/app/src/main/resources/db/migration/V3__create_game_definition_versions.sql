-- GameDefinitionVersion: snapshot versionado e inmutable (tras publicarse)
-- del contenido real de un juego (cartas, mazos, zonas, turnos, fases,
-- reglas, condiciones de victoria), expresado como config declarativa (JSON).
-- Mientras published_at es NULL, la versión es un borrador editable.
CREATE TABLE game_definition_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_definition_id  UUID NOT NULL REFERENCES game_definitions(id),
    version_number      INTEGER NOT NULL,
    config              JSONB NOT NULL,
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_game_definition_versions_number UNIQUE (game_definition_id, version_number)
);

CREATE INDEX idx_game_definition_versions_definition_id
    ON game_definition_versions (game_definition_id);
