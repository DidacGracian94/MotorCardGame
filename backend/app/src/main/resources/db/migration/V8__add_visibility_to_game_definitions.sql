-- Visibilidad de la GameDefinition frente a otros usuarios: PRIVATE (solo el dueño y los admins la
-- ven) o PUBLIC (cualquier usuario autenticado la ve, pero solo el dueño o un admin puede editarla).
ALTER TABLE game_definitions ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE';
