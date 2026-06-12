-- =========================================================
-- MODULE USER PROFILE
-- =========================================================

CREATE TABLE importer_profiles (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    account_id        CHAR(36)     NOT NULL UNIQUE COMMENT 'Référence vers accounts.id',
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    phone_number      VARCHAR(30),
    country           VARCHAR(100),
    city              VARCHAR(100),
    importer_type     ENUM('BEGINNER','SME','ECOMMERCE','LARGE_IMPORTER') DEFAULT 'BEGINNER',
    profile_photo_url VARCHAR(500),
    is_complete       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_importer_account (account_id)
);

CREATE TABLE agent_profiles (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    account_id        CHAR(36)     NOT NULL UNIQUE,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    phone_number      VARCHAR(30),
    position          VARCHAR(150) COMMENT 'Poste occupé',
    profile_photo_url VARCHAR(500),
    forwarder_id      CHAR(36)     COMMENT 'Transitaire de rattachement',
    is_complete       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_agent_account   (account_id),
    INDEX idx_agent_forwarder (forwarder_id)
);

CREATE TABLE admin_forwarder_profiles (
    id                     CHAR(36)     NOT NULL PRIMARY KEY,
    account_id             CHAR(36)     NOT NULL UNIQUE,
    first_name             VARCHAR(100),
    last_name              VARCHAR(100),
    phone_number           VARCHAR(30),
    operational_position   VARCHAR(150),
    forwarder_id           CHAR(36)     NOT NULL,
    additional_permissions SET('ACCOUNTANT','AGENT','CONTROLLER'),
    is_complete            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_admin_fwd_account    (account_id),
    INDEX idx_admin_fwd_forwarder  (forwarder_id)
);

CREATE TABLE super_admin_profiles (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    account_id        CHAR(36)     NOT NULL UNIQUE,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    phone_number      VARCHAR(30),
    super_role        ENUM('DEVELOPER','COMMERCIAL','ACCOUNTANT','SUPER_ADMIN'),
    is_complete       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_super_admin_account (account_id)
);

CREATE TABLE gdpr_requests (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    account_id       CHAR(36)     NOT NULL,
    request_type     ENUM('DELETION','EXPORT') NOT NULL,
    status           ENUM('PENDING','PROCESSING','COMPLETED','REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at     TIMESTAMP    NULL,
    processed_by     CHAR(36)     NULL COMMENT 'super_admin account_id',
    rejection_reason VARCHAR(500),
    INDEX idx_gdpr_account (account_id)
);
