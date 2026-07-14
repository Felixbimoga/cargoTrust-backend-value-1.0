package com.gargotrust.gestion_achats_enligne.forwarder.domain.enums;

/** Cycle de vérification d'un transitaire (filtre l'informel). */
public enum VerificationStatus {
    DRAFT,        // En cours de saisie
    SUBMITTED,    // Soumis, en attente de validation
    VERIFIED,     // Validé
    REJECTED      // Rejeté (voir rejectionReason)
}
