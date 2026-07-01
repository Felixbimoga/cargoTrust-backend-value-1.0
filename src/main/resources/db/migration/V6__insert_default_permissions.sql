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

-- ROLE_CLIENT (importateur)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_CLIENT'
  AND p.name IN ('orders:create','orders:read','orders:cancel','shipments:read',
                 'proofs:read','payments:read','payments:initiate','incidents:read');

-- ROLE_TRANSITAIRE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_TRANSITAIRE'
  AND p.name IN ('orders:read','shipments:read','shipments:update',
                 'proofs:create','proofs:read','incidents:read');

-- ROLE_ADMIN_TRANSITAIRE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_ADMIN_TRANSITAIRE'
  AND p.name IN ('orders:read_all','orders:update','shipments:read','shipments:update',
                 'proofs:read','payments:read','payments:validate','users:read',
                 'incidents:read','incidents:manage');

-- ROLE_SUPER_ADMIN : toutes les permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON 1=1
WHERE r.name = 'ROLE_SUPER_ADMIN';
