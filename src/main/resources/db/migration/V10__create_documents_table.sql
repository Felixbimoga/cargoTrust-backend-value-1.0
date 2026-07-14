-- =========================================================
-- V10 : Documents KYC polymorphes (module document)
-- Rattachés à tout porteur via owner_type + owner_id.
-- =========================================================

CREATE TABLE documents (
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    owner_type        VARCHAR(30)  NOT NULL COMMENT 'FORWARDER, ACCOUNT, …',
    owner_id          CHAR(36)     NOT NULL,
    doc_type          VARCHAR(40)  NOT NULL COMMENT 'REPRESENTATIVE_ID, RCCM, TAX_CLEARANCE, CUSTOMS_LICENSE, OTHER',
    file_url          VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    content_type      VARCHAR(100),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, VERIFIED, REJECTED',
    expires_at        DATE         COMMENT 'Ex : patente valable pour l''année en cours',
    rejection_reason  VARCHAR(500),
    verified_by       CHAR(36)     COMMENT 'Compte administrateur ayant vérifié',
    verified_at       TIMESTAMP    NULL DEFAULT NULL,
    uploaded_by       CHAR(36),
    uploaded_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_document_owner  (owner_type, owner_id),
    INDEX idx_document_status (status)
);
