-- =========================================================
-- V5 : Table unique de profils utilisateurs
-- =========================================================

CREATE TABLE user_profiles (
    id                CHAR(36)  NOT NULL PRIMARY KEY,
    account_id        CHAR(36)  NOT NULL UNIQUE COMMENT 'FK vers accounts.id',
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    phone_number      VARCHAR(30),
    country           VARCHAR(100),
    city              VARCHAR(100),
    profile_photo_url VARCHAR(500),
    bio               TEXT,
    is_complete       BOOLEAN   NOT NULL DEFAULT FALSE,
    role_metadata     JSON      COMMENT 'Champs spécifiques au rôle (importerType, position, etc.)',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_profile_account (account_id),
    CONSTRAINT fk_profile_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
