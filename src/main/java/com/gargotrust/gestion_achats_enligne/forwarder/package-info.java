/**
 * Module Forwarder (Transitaire / Commissionnaire en douane).
 *
 * Gère l'agrégat « entreprise de transit » : identité légale et fiscale (NIU, RCCM,
 * agrément douane), localisation (siège + hubs en Chine), capacités opérationnelles
 * (origines, destinations, modes de transport, spécialisations), assurance, charte
 * de transparence et cycle de vérification.
 *
 * Un transitaire est partagé par plusieurs comptes IAM via {@code ForwarderMember}
 * (OWNER = ADMIN_TRANSITAIRE, AGENT = TRANSITAIRE) : les données légales de
 * l'entreprise sont stockées une seule fois.
 *
 * Dépendances autorisées :
 *  - shared.security  (CurrentUserContext)
 *  - shared.service   (StorageService)
 *  - shared.exception (DomainException)
 * Aucun accès direct aux internes du module IAM (référence les comptes par UUID).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Forwarder — Transitaires"
)
package com.gargotrust.gestion_achats_enligne.forwarder;
