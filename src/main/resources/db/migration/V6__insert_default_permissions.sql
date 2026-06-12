-- =========================================================
-- V6 : Permissions par défaut et attribution aux rôles
-- =========================================================

INSERT INTO permissions (name, resource, action, description) VALUES
('orders:create',      'orders',     'create',   'Créer une commande'),
('orders:read',        'orders',     'read',     'Consulter ses commandes'),
('orders:read_all',    'orders',     'read_all', 'Consulter toutes les commandes'),
('orders:update',      'orders',     'update',   'Modifier une commande'),
('orders:cancel',      'orders',     'cancel',   'Annuler une commande'),
('shipments:read',     'shipments',  'read',     'Consulter les expéditions'),
('shipments:update',   'shipments',  'update',   'Mettre à jour une expédition'),
('proofs:create',      'proofs',     'create',   'Créer une preuve numérique'),
('proofs:read',        'proofs',     'read',     'Consulter les preuves'),
('payments:read',      'payments',   'read',     'Consulter les paiements'),
('payments:initiate',  'payments',   'initiate', 'Initier un paiement'),
('payments:validate',  'payments',   'validate', 'Valider un paiement'),
('users:read',         'users',      'read',     'Consulter la liste des utilisateurs'),
('users:manage',       'users',      'manage',   'Gérer les comptes utilisateurs'),
('forwarders:read',    'forwarders', 'read',     'Consulter les transitaires'),
('forwarders:manage',  'forwarders', 'manage',   'Gérer les transitaires'),
('analytics:read',     'analytics',  'read',     'Consulter les tableaux de bord'),
('incidents:read',     'incidents',  'read',     'Consulter les incidents'),
('incidents:manage',   'incidents',  'manage',   'Gérer les incidents');

-- ROLE_IMPORTER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_IMPORTER'
  AND p.name IN ('orders:create','orders:read','orders:cancel','shipments:read',
                 'proofs:read','payments:read','payments:initiate','incidents:read');

-- ROLE_AGENT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_AGENT'
  AND p.name IN ('orders:read','shipments:read','shipments:update',
                 'proofs:create','proofs:read','incidents:read');

-- ROLE_ADMIN_FORWARDER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_ADMIN_FORWARDER'
  AND p.name IN ('orders:read_all','orders:update','shipments:read','shipments:update',
                 'proofs:read','payments:read','payments:validate','users:read',
                 'incidents:read','incidents:manage');

-- ROLE_SUPER_RESPONSIBLE : toutes les permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_RESPONSIBLE';

-- ROLE_SUPER_COMMERCIAL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_COMMERCIAL'
  AND p.name IN ('users:read','users:manage','forwarders:read','forwarders:manage',
                 'orders:read_all','analytics:read');

-- ROLE_SUPER_FINANCIAL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_FINANCIAL'
  AND p.name IN ('payments:read','payments:validate','orders:read_all',
                 'analytics:read','users:read');

-- ROLE_SUPER_PACKAGE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_PACKAGE'
  AND p.name IN ('shipments:read','shipments:update','proofs:read','orders:read_all',
                 'analytics:read','incidents:read','incidents:manage');
