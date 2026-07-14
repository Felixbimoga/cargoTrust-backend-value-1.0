package com.gargotrust.gestion_achats_enligne.importer.domain.enums;

/** Fréquence estimée des importations. */
public enum ImportFrequency {
    MULTIPLE_TIMES_PER_MONTH,   // Plusieurs fois par mois
    ONCE_PER_MONTH,             // Une fois par mois
    QUARTERLY,                  // Chaque trimestre (saisonnier)
    RARELY                      // Rarement / occasionnellement
}
