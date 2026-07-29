package com.gargotrust.gestion_achats_enligne.iam.config;

import com.gargotrust.gestion_achats_enligne.iam.domain.Permission;
import com.gargotrust.gestion_achats_enligne.iam.domain.Permissions;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.domain.RolePermission;
import com.gargotrust.gestion_achats_enligne.iam.repository.PermissionRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RolePermissionRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seed idempotent du référentiel RBAC (permissions + attribution aux rôles).
 *
 * <p>Les scripts Flyway V6/V8 font la même chose, mais le profil {@code dev} désactive Flyway
 * (schéma généré par {@code ddl-auto: update}) : sans ce seeder les tables {@code permissions}
 * et {@code role_permissions} restent vides et tous les {@code hasAuthority(...)} échouent,
 * super admin compris.</p>
 *
 * <p>Exécuté à chaque démarrage : les permissions et attributions manquantes sont créées,
 * les existantes sont laissées intactes (aucune révocation). Une nouvelle permission ajoutée
 * dans {@link Permissions} est donc automatiquement propagée au super admin.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionInitializer {

    private final PermissionRepository     permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository           roleRepository;

    /** Catalogue de référence : nom de permission -> description. */
    private static final Map<String, String> PERMISSION_CATALOG = new LinkedHashMap<>();
    static {
        PERMISSION_CATALOG.put(Permissions.ORDERS_CREATE,      "Créer une commande");
        PERMISSION_CATALOG.put(Permissions.ORDERS_READ,        "Consulter ses commandes");
        PERMISSION_CATALOG.put(Permissions.ORDERS_READ_ALL,    "Consulter toutes les commandes");
        PERMISSION_CATALOG.put(Permissions.ORDERS_UPDATE,      "Modifier une commande");
        PERMISSION_CATALOG.put(Permissions.ORDERS_CANCEL,      "Annuler une commande");
        PERMISSION_CATALOG.put(Permissions.SHIPMENTS_READ,     "Consulter les expéditions");
        PERMISSION_CATALOG.put(Permissions.SHIPMENTS_UPDATE,   "Mettre à jour une expédition");
        PERMISSION_CATALOG.put(Permissions.PROOFS_CREATE,      "Créer une preuve numérique");
        PERMISSION_CATALOG.put(Permissions.PROOFS_READ,        "Consulter les preuves");
        PERMISSION_CATALOG.put(Permissions.PAYMENTS_READ,      "Consulter les paiements");
        PERMISSION_CATALOG.put(Permissions.PAYMENTS_INITIATE,  "Initier un paiement");
        PERMISSION_CATALOG.put(Permissions.PAYMENTS_VALIDATE,  "Valider un paiement");
        PERMISSION_CATALOG.put(Permissions.USERS_READ,         "Consulter la liste des utilisateurs");
        PERMISSION_CATALOG.put(Permissions.USERS_MANAGE,       "Gérer les comptes utilisateurs");
        PERMISSION_CATALOG.put(Permissions.FORWARDERS_READ,    "Consulter les transitaires");
        PERMISSION_CATALOG.put(Permissions.FORWARDERS_MANAGE,  "Gérer les transitaires");
        PERMISSION_CATALOG.put(Permissions.CATALOG_READ,       "Consulter les référentiels catalogue");
        PERMISSION_CATALOG.put(Permissions.CATALOG_MANAGE,     "Gérer les catégories de produits");
        PERMISSION_CATALOG.put(Permissions.ANALYTICS_READ,     "Consulter les tableaux de bord");
        PERMISSION_CATALOG.put(Permissions.INCIDENTS_READ,     "Consulter les incidents");
        PERMISSION_CATALOG.put(Permissions.INCIDENTS_MANAGE,   "Gérer les incidents");
    }

    /** Attributions par rôle. Le super admin reçoit tout le catalogue (cf. grantAll). */
    private static final Map<String, Set<String>> ROLE_GRANTS = Map.of(
            Role.CLIENT, Set.of(
                    Permissions.ORDERS_CREATE, Permissions.ORDERS_READ, Permissions.ORDERS_CANCEL,
                    Permissions.SHIPMENTS_READ, Permissions.PROOFS_READ,
                    Permissions.PAYMENTS_READ, Permissions.PAYMENTS_INITIATE,
                    Permissions.INCIDENTS_READ),

            Role.TRANSITAIRE, Set.of(
                    Permissions.ORDERS_READ, Permissions.SHIPMENTS_READ, Permissions.SHIPMENTS_UPDATE,
                    Permissions.PROOFS_CREATE, Permissions.PROOFS_READ,
                    Permissions.INCIDENTS_READ),

            Role.ADMIN_TRANSITAIRE, Set.of(
                    Permissions.ORDERS_READ_ALL, Permissions.ORDERS_UPDATE,
                    Permissions.SHIPMENTS_READ, Permissions.SHIPMENTS_UPDATE,
                    Permissions.PROOFS_READ, Permissions.PAYMENTS_READ, Permissions.PAYMENTS_VALIDATE,
                    Permissions.USERS_READ, Permissions.INCIDENTS_READ, Permissions.INCIDENTS_MANAGE),

            Role.SUPER_ADMIN, PERMISSION_CATALOG.keySet()
    );

    /** Appelé par {@link DataInitializer} après la création des rôles. */
    public void seed() {
        Map<String, Permission> permissions = ensurePermissionsExist();
        ROLE_GRANTS.forEach((roleName, granted) -> ensureGrants(roleName, granted, permissions));
    }

    private Map<String, Permission> ensurePermissionsExist() {
        Map<String, Permission> byName = new LinkedHashMap<>();
        int created = 0;

        for (Map.Entry<String, String> entry : PERMISSION_CATALOG.entrySet()) {
            String name = entry.getKey();
            Optional<Permission> existing = permissionRepository.findByName(name);
            if (existing.isPresent()) {
                byName.put(name, existing.get());
                continue;
            }
            String[] parts = name.split(":", 2);
            Permission permission = permissionRepository.save(Permission.builder()
                    .name(name)
                    .resource(parts[0])
                    .action(parts[1])
                    .description(entry.getValue())
                    .build());
            byName.put(name, permission);
            created++;
        }

        if (created > 0) {
            log.info("{} permission(s) créée(s) dans le référentiel RBAC.", created);
        }
        return byName;
    }

    private void ensureGrants(String roleName, Set<String> grantedNames, Map<String, Permission> permissions) {
        Optional<Role> role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            log.warn("Rôle {} introuvable, attribution des permissions ignorée.", roleName);
            return;
        }

        List<RolePermission> current = rolePermissionRepository.findAllByRoleId(role.get().getId());
        Set<Long> alreadyGranted = current.stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        int created = 0;
        for (String name : grantedNames) {
            Permission permission = permissions.get(name);
            if (permission == null || alreadyGranted.contains(permission.getId())) {
                continue;
            }
            rolePermissionRepository.save(RolePermission.builder()
                    .role(role.get())
                    .permission(permission)
                    .build());
            created++;
        }

        if (created > 0) {
            log.info("{} permission(s) attribuée(s) au rôle {}.", created, roleName);
        }
    }
}
