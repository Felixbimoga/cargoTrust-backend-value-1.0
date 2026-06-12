CREATE TABLE accounts (
    id            CHAR(36) PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status        ENUM('ACTIVE','SUSPENDED','PENDING_VERIFICATION') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL
);

CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE account_roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id  CHAR(36) NOT NULL,
    role_id     BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_role    FOREIGN KEY (role_id)    REFERENCES roles(id),
    CONSTRAINT uq_account_role UNIQUE (account_id, role_id)
);

CREATE TABLE otp_tokens (
    id         CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    otp_hash   VARCHAR(255) NOT NULL,
    type       ENUM('REGISTRATION','LOGIN','PASSWORD_RESET') NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed   TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_account_type (account_id, type)
);

CREATE TABLE refresh_tokens (
    id            CHAR(36) PRIMARY KEY,
    account_id    CHAR(36) NOT NULL,
    token_hash    VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMP NOT NULL,
    revoked       TINYINT(1) NOT NULL DEFAULT 0,
    last_otp_date DATE NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent    VARCHAR(500) NULL,
    ip_address    VARCHAR(45) NULL,
    INDEX idx_refresh_account (account_id)
);

CREATE TABLE login_sessions (
    id          CHAR(36) PRIMARY KEY,
    account_id  CHAR(36) NOT NULL,
    ip_address  VARCHAR(45) NULL,
    user_agent  VARCHAR(500) NULL,
    device_type ENUM('MOBILE','WEB') NULL,
    login_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    successful  TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_session_account (account_id)
);

CREATE TABLE password_reset_tokens (
    id         CHAR(36) PRIMARY KEY,
    account_id CHAR(36) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used       TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
