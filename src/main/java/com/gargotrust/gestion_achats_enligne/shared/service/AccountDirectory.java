package com.gargotrust.gestion_achats_enligne.shared.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'annuaire des comptes IAM exposé aux autres modules.
 * Résout des informations d'affichage (email, nom) à partir d'un {@code accountId},
 * pour enrichir les réponses des modules qui ne référencent les comptes que par UUID.
 */
public interface AccountDirectory {

    Optional<AccountSummary> find(UUID accountId);

    /** Résolution en lot (évite le N+1). Les identifiants inconnus sont absents de la map. */
    Map<UUID, AccountSummary> findAll(Collection<UUID> accountIds);

    record AccountSummary(UUID accountId, String email, String firstName, String lastName) {}
}
