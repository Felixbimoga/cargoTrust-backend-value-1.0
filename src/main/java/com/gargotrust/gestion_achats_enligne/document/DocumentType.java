package com.gargotrust.gestion_achats_enligne.document;

/** Nature d'une pièce justificative KYC. */
public enum DocumentType {
    REPRESENTATIVE_ID,   // Pièce d'identité du représentant (CNI, passeport, carte de séjour)
    RCCM,                // Registre du Commerce et du Crédit Mobilier
    TAX_CLEARANCE,       // Patente / attestation de non-redevance fiscale
    CUSTOMS_LICENSE,     // Arrêté d'agrément de commissionnaire en douane
    OTHER
}
