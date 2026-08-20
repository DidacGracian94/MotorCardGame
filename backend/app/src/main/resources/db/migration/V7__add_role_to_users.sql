-- Rol de la cuenta: ADMIN ve/edita todo, USER solo lo suyo + lo público (ver game_definitions).
-- El primer admin se promueve a mano (UPDATE); a partir de ahí un admin promueve a otros vía API.
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
