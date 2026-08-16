-- GameDefinition: identidad y metadata de un juego configurable.
-- El propio config del juego (reglas, cartas, zonas...) NO vive aquí:
-- vive versionado en game_definition_versions (JSONB), porque una
-- GameDefinition es inmutable una vez publicada una versión.
CREATE TABLE game_definitions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID NOT NULL,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_game_definitions_owner_slug UNIQUE (owner_id, slug)
);
