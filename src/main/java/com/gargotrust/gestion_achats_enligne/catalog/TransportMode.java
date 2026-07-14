package com.gargotrust.gestion_achats_enligne.catalog;

/**
 * Modes de transport — taxonomie partagée (offre transitaire ↔ besoin importateur),
 * socle du futur matching. Exposée par le module catalog.
 */
public enum TransportMode {
    FRET_MARITIME_FCL,   // Conteneur complet
    FRET_MARITIME_LCL,   // Groupage (cœur de cible)
    FRET_AERIEN          // Cargo / colis express
}
