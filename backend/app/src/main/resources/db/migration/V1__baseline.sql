-- Baseline: extensión necesaria para generar UUIDs como identidad de las entidades del dominio
-- (game_definitions, game_definition_versions, game_instances, ...) que se añadirán en migraciones futuras.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
