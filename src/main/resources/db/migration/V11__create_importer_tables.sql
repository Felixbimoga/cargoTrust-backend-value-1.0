-- =========================================================
-- V11 : Module Importer (profil importateur — rôle CLIENT)
-- Dépend de V8 (addresses, product_categories).
-- =========================================================

CREATE TABLE importer_profiles (
    id                                CHAR(36)     NOT NULL PRIMARY KEY,
    account_id                        CHAR(36)     NOT NULL UNIQUE COMMENT 'FK vers accounts.id',

    -- Nature de l'activité
    importer_type                     VARCHAR(30)  COMMENT 'PARTICULIER, COMMERCANT, PME',
    business_name                     VARCHAR(200) COMMENT 'Raison sociale / nom de la boutique',
    tax_id                            VARCHAR(100),

    -- Habitudes
    import_frequency                  VARCHAR(30)  COMMENT 'MULTIPLE_TIMES_PER_MONTH, ONCE_PER_MONTH, QUARTERLY, RARELY',

    -- Escrow
    escrow_enabled                    BOOLEAN      NOT NULL DEFAULT FALSE,
    refund_method                     VARCHAR(30)  COMMENT 'MOBILE_MONEY, BANK_TRANSFER, NEXT_SHIPMENT_CREDIT',

    -- Livraison
    delivery_address_id               CHAR(36),
    delivery_country_code             VARCHAR(3),

    -- Charte d'achat sécurisé
    secured_purchase_charter_accepted BOOLEAN      NOT NULL DEFAULT FALSE,
    charter_accepted_at               TIMESTAMP    NULL DEFAULT NULL,

    is_complete                       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at                        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_importer_country (delivery_country_code),
    CONSTRAINT fk_importer_account  FOREIGN KEY (account_id)          REFERENCES accounts(id)  ON DELETE CASCADE,
    CONSTRAINT fk_importer_delivery FOREIGN KEY (delivery_address_id) REFERENCES addresses(id) ON DELETE SET NULL
);

-- ── Besoins (taxonomies queryables — socle du matching) ───────────────────────
CREATE TABLE importer_categories (
    importer_profile_id CHAR(36) NOT NULL,
    product_category_id BIGINT   NOT NULL,
    PRIMARY KEY (importer_profile_id, product_category_id),
    CONSTRAINT fk_ic_importer FOREIGN KEY (importer_profile_id) REFERENCES importer_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ic_category FOREIGN KEY (product_category_id) REFERENCES product_categories(id)
);

CREATE TABLE importer_transport_modes (
    importer_profile_id CHAR(36)    NOT NULL,
    transport_mode      VARCHAR(40) NOT NULL,
    PRIMARY KEY (importer_profile_id, transport_mode),
    CONSTRAINT fk_itm_importer FOREIGN KEY (importer_profile_id) REFERENCES importer_profiles(id) ON DELETE CASCADE
);

CREATE TABLE importer_origins (
    importer_profile_id CHAR(36)   NOT NULL,
    origin_country      VARCHAR(3) NOT NULL,
    PRIMARY KEY (importer_profile_id, origin_country),
    CONSTRAINT fk_io_importer FOREIGN KEY (importer_profile_id) REFERENCES importer_profiles(id) ON DELETE CASCADE
);

CREATE TABLE importer_selection_priorities (
    importer_profile_id CHAR(36)    NOT NULL,
    priority_order      INT         NOT NULL,
    criterion           VARCHAR(20) NOT NULL COMMENT 'PRICE, SECURITY, SPEED',
    PRIMARY KEY (importer_profile_id, priority_order),
    CONSTRAINT fk_isp_importer FOREIGN KEY (importer_profile_id) REFERENCES importer_profiles(id) ON DELETE CASCADE
);
