-- =========================================================
-- V4 : Suppression des anciennes tables de profil (V3),
--      mise à jour du schéma RBAC, ajout permissions
-- =========================================================

-- Suppression des 5 tables créées en V3
DROP TABLE IF EXISTS gdpr_requests;
DROP TABLE IF EXISTS admin_forwarder_profiles;
DROP TABLE IF EXISTS agent_profiles;
DROP TABLE IF EXISTS importer_profiles;
DROP TABLE IF EXISTS super_admin_profiles;

-- Enrichissement de la table roles
ALTER TABLE roles
    ADD COLUMN display_name VARCHAR(150) NULL AFTER name,
    ADD COLUMN is_system    BOOLEAN NOT NULL DEFAULT TRUE AFTER description;

UPDATE roles SET display_name = 'Super Admin'       WHERE name = 'ROLE_SUPER_ADMIN';
UPDATE roles SET display_name = 'Admin Transitaire' WHERE name = 'ROLE_ADMIN_TRANSITAIRE';
UPDATE roles SET display_name = 'Transitaire'       WHERE name = 'ROLE_TRANSITAIRE';
UPDATE roles SET display_name = 'Client'            WHERE name = 'ROLE_CLIENT';

-- Table des permissions atomiques
CREATE TABLE permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE COMMENT 'Format resource:action ex: orders:read',
    resource    VARCHAR(50)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    UNIQUE KEY uq_perm_resource_action (resource, action)
);

-- Mapping rôle <-> permissions
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
