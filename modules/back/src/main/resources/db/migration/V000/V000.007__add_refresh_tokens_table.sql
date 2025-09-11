CREATE TABLE IF NOT EXISTS mddsocial.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES mddsocial.users(id) ON DELETE CASCADE
);

-- Création d'index pour améliorer les performances des requêtes
CREATE INDEX idx_refresh_token ON mddsocial.refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON mddsocial.refresh_tokens(user_id);

COMMENT ON TABLE mddsocial.refresh_tokens IS 'Stocke les refresh tokens pour permettre le renouvellement des access tokens';
COMMENT ON COLUMN mddsocial.refresh_tokens.id IS 'Identifiant unique du refresh token';
COMMENT ON COLUMN mddsocial.refresh_tokens.user_id IS 'Identifiant de l''utilisateur associé au refresh token';
COMMENT ON COLUMN mddsocial.refresh_tokens.token IS 'Valeur du refresh token';
COMMENT ON COLUMN mddsocial.refresh_tokens.expiry_date IS 'Date d''expiration du refresh token';