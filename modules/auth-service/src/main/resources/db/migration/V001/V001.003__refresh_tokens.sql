-- Table des refresh tokens (stockage côté serveur)
CREATE TABLE IF NOT EXISTS mddauth.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id)
        REFERENCES mddauth.users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_user_id ON mddauth.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_expires_at ON mddauth.refresh_tokens(expires_at);
