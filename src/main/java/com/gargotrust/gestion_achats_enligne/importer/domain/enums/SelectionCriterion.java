package com.gargotrust.gestion_achats_enligne.importer.domain.enums;

/** Critère de sélection d'un transitaire (classé par ordre de priorité). */
public enum SelectionCriterion {
    PRICE,      // Le prix au m³ / kilo
    SECURITY,   // Sécurité & Cargo-Score (zéro perte, transparence)
    SPEED       // Rapidité (respect des délais)
}
