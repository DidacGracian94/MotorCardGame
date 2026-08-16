-- GameInstance: una partida concreta creada a partir de una GameDefinitionVersion fija.
-- game_definition_id va denormalizado junto a game_definition_version_id para poder listar
-- instancias por definición sin join.
CREATE TABLE game_instances (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_definition_id          UUID NOT NULL REFERENCES game_definitions(id),
    game_definition_version_id  UUID NOT NULL REFERENCES game_definition_versions(id),
    state                       JSONB NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at                    TIMESTAMPTZ
);

CREATE INDEX idx_game_instances_definition_id
    ON game_instances (game_definition_id);

CREATE INDEX idx_game_instances_version_id
    ON game_instances (game_definition_version_id);
