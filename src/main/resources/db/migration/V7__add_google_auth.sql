-- =========================================================
-- V7 : Support authentification Google OAuth2
-- =========================================================

-- Rend password_hash nullable pour les comptes Google
ALTER TABLE accounts
    MODIFY COLUMN password_hash VARCHAR(255) NULL;

-- Ajoute la colonne google_id (sub Google, unique par compte)
ALTER TABLE accounts
    ADD COLUMN google_id VARCHAR(255) NULL UNIQUE AFTER password_hash;
