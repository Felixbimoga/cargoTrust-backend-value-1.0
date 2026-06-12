package com.gargotrust.gestion_achats_enligne.shared.security;

import java.util.List;
import java.util.UUID;

/**
 * Interface publique partagée avec tous les modules.
 * Seule dépendance autorisée vers le module IAM depuis les autres modules.
 */
public interface CurrentUserContext {
    UUID         getAccountId();
    String       getEmail();
    String       getRole();
    List<String> getPermissions();
    boolean      hasRole(String role);
    boolean      hasPermission(String permission);
    boolean      isAuthenticated();
}
