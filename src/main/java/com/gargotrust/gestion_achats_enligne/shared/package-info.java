/**
 * Shared Kernel — briques transverses partagées par tous les modules métier.
 *
 * Déclaré {@code OPEN} : les autres modules (iam, forwarder, …) peuvent accéder
 * librement à ses sous-packages :
 *  - shared.security  → CurrentUserContext (contexte utilisateur courant)
 *  - shared.events    → événements de domaine inter-modules
 *  - shared.service   → services d'infrastructure (EmailService, StorageService)
 *  - shared.exception → ApiError, GlobalExceptionHandler, exceptions transverses
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN,
    displayName = "Shared Kernel"
)
package com.gargotrust.gestion_achats_enligne.shared;
