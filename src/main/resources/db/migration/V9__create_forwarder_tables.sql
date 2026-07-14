-- =========================================================
-- V9 : Module Forwarder (transitaires / commissionnaires en douane)
-- Dépend de V8 (addresses, product_categories).
-- =========================================================

-- ── Entreprise de transit (agrégat racine) ───────────────────────────────────
CREATE TABLE forwarders (
    id                            CHAR(36)     NOT NULL PRIMARY KEY,

    -- Profil entreprise
    legal_name                    VARCHAR(200) NOT NULL COMMENT 'Raison sociale',
    structure_type                VARCHAR(40)  COMMENT 'SARL, SA, ENTREPRISE_INDIVIDUELLE, GIE, AUTRE',
    creation_date                 DATE,
    employee_count                INT,
    logo_url                      VARCHAR(500),
    cover_photo_url               VARCHAR(500) COMMENT 'Photo de couverture',
    website_url                   VARCHAR(500),

    -- Fiscal & légal (CEMAC / CEDEAO)
    tax_id                        VARCHAR(100) COMMENT 'NIU / NIF',
    rccm                          VARCHAR(100) COMMENT 'Registre du Commerce',
    customs_license_number        VARCHAR(100) COMMENT 'N° agrément commissionnaire en douane',

    -- Localisation siège (adresse géolocalisable + code pays dénormalisé)
    headquarters_address_id       CHAR(36),
    headquarters_country_code     VARCHAR(3),

    -- Présence d'entrepôts à l'origine
    has_origin_warehouse          BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Opérationnel
    departure_frequency           VARCHAR(20),
    variable_schedule             BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Assurance
    insurance_offered             BOOLEAN      NOT NULL DEFAULT FALSE,
    insurance_coverage_rate       VARCHAR(200),
    insurance_company             VARCHAR(200),

    -- Digitalisation (équipe à l'origine)
    origin_team_languages         VARCHAR(200),
    origin_team_equipped          BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Charte de transparence
    transparency_charter_accepted BOOLEAN      NOT NULL DEFAULT FALSE,
    charter_accepted_at           TIMESTAMP    NULL DEFAULT NULL,

    -- Cycle de vérification
    verification_status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    rejection_reason              VARCHAR(500),

    created_at                    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_forwarder_status  (verification_status),
    INDEX idx_forwarder_country (headquarters_country_code),
    CONSTRAINT fk_forwarder_hq_address FOREIGN KEY (headquarters_address_id) REFERENCES addresses(id) ON DELETE SET NULL
);

-- ── Rattachement compte IAM ↔ entreprise (OWNER / AGENT) ──────────────────────
CREATE TABLE forwarder_members (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    forwarder_id  CHAR(36)     NOT NULL,
    account_id    CHAR(36)     NOT NULL,
    member_role   VARCHAR(20)  NOT NULL COMMENT 'OWNER, AGENT',
    position      VARCHAR(150) COMMENT 'Fonction : DG, Responsable Logistique, Déclarant…',
    joined_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_member_account UNIQUE (account_id),
    INDEX idx_member_forwarder (forwarder_id),
    CONSTRAINT fk_member_forwarder FOREIGN KEY (forwarder_id) REFERENCES forwarders(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_account   FOREIGN KEY (account_id)   REFERENCES accounts(id)   ON DELETE CASCADE
);

-- ── Entrepôts à l'origine (1:N), adresse géolocalisable ───────────────────────
CREATE TABLE forwarder_warehouses (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    forwarder_id  CHAR(36)     NOT NULL,
    label         VARCHAR(150),
    address_id    CHAR(36),

    INDEX idx_warehouse_forwarder (forwarder_id),
    CONSTRAINT fk_warehouse_forwarder FOREIGN KEY (forwarder_id) REFERENCES forwarders(id) ON DELETE CASCADE,
    CONSTRAINT fk_warehouse_address   FOREIGN KEY (address_id)   REFERENCES addresses(id)  ON DELETE SET NULL
);

-- ── Capacités (taxonomies queryables — socle du matching) ─────────────────────
CREATE TABLE forwarder_transport_modes (
    forwarder_id   CHAR(36)    NOT NULL,
    transport_mode VARCHAR(40) NOT NULL,
    PRIMARY KEY (forwarder_id, transport_mode),
    CONSTRAINT fk_ftm_forwarder FOREIGN KEY (forwarder_id) REFERENCES forwarders(id) ON DELETE CASCADE
);

CREATE TABLE forwarder_origins (
    forwarder_id   CHAR(36)   NOT NULL,
    origin_country VARCHAR(3) NOT NULL,
    PRIMARY KEY (forwarder_id, origin_country),
    CONSTRAINT fk_fo_forwarder FOREIGN KEY (forwarder_id) REFERENCES forwarders(id) ON DELETE CASCADE
);

CREATE TABLE forwarder_destinations (
    forwarder_id        CHAR(36)   NOT NULL,
    destination_country VARCHAR(3) NOT NULL,
    PRIMARY KEY (forwarder_id, destination_country),
    CONSTRAINT fk_fd_forwarder FOREIGN KEY (forwarder_id) REFERENCES forwarders(id) ON DELETE CASCADE
);

CREATE TABLE forwarder_specializations (
    forwarder_id        CHAR(36) NOT NULL,
    product_category_id BIGINT   NOT NULL,
    PRIMARY KEY (forwarder_id, product_category_id),
    CONSTRAINT fk_fs_forwarder FOREIGN KEY (forwarder_id)        REFERENCES forwarders(id)        ON DELETE CASCADE,
    CONSTRAINT fk_fs_category   FOREIGN KEY (product_category_id) REFERENCES product_categories(id)
);
