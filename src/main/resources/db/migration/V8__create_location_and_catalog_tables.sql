-- =========================================================
-- V8 : Socle transverse — Adresses (location) & Catégories produits (catalog)
-- =========================================================

-- ── Adresses globales (siège, entrepôts, livraisons…) avec géolocalisation ────
CREATE TABLE addresses (
    id            CHAR(36)      NOT NULL PRIMARY KEY,
    country_code  VARCHAR(3)    COMMENT 'ISO2 / ISO3 (référentiel countriesnow)',
    country_name  VARCHAR(120),
    state_region  VARCHAR(150),
    city          VARCHAR(150),
    neighborhood  VARCHAR(150)  COMMENT 'Quartier',
    line          VARCHAR(300)  COMMENT 'Rue, n°, complément',
    latitude      DECIMAL(10,7),
    longitude     DECIMAL(10,7),
    place_label   VARCHAR(255),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_address_country (country_code)
);

-- ── Catégories de produits (administrables ; coefficient pour calcul de coût) ──
CREATE TABLE product_categories (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(60)   NOT NULL UNIQUE,
    name             VARCHAR(150)  NOT NULL,
    description      VARCHAR(500),
    cost_coefficient DECIMAL(6,3)  NOT NULL DEFAULT 1.000 COMMENT 'Facteur de calcul de coût de transport',
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO product_categories (code, name, description, cost_coefficient) VALUES
('ELECTRONICS_HIGHTECH',   'Électronique & High-Tech',            'Smartphones, écouteurs, accessoires audio, smartwatches', 1.200),
('FASHION_COSMETICS',      'Prêt-à-porter, Chaussures & Cosmétiques', 'Mode, chaussures, produits de beauté',                1.000),
('AUTO_PARTS_TOOLS',       'Pièces détachées & Outillage',        'Pièces automobiles, outillage industriel',                1.300),
('PERISHABLES_FOOD',       'Produits périssables / Agroalimentaire', 'Denrées périssables et agroalimentaires',              1.500),
('CONSTRUCTION_MATERIALS', 'Matériaux de construction',           'Quincaillerie et matériaux de construction',              1.400);

-- ── Permission d'administration du catalogue + attribution au super admin ─────
INSERT INTO permissions (name, resource, action, description) VALUES
('catalog:read',   'catalog', 'read',   'Consulter les référentiels catalogue'),
('catalog:manage', 'catalog', 'manage', 'Gérer les catégories de produits');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_ADMIN'
  AND p.name IN ('catalog:read', 'catalog:manage');
