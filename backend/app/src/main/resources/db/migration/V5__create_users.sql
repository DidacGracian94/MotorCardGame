-- Users: cuentas de acceso al frontal. Una cuenta puede tener contraseña propia (registro
-- email+password), estar ligada a Google (google_subject = claim "sub" del id_token), o ambas —
-- por eso password_hash y google_subject son nullable, no mutuamente excluyentes.
CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255),
    google_subject VARCHAR(255),
    display_name   VARCHAR(120) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_google_subject UNIQUE (google_subject)
);
