/**
 * Module Document — pièces justificatives (KYC) polymorphes et globales.
 *
 * Une table {@code documents} unique dessert tout porteur de documents via
 * {@code owner_type} + {@code owner_id} : transitaire aujourd'hui (CNI du représentant,
 * RCCM, patente, arrêté d'agrément), puis compte / expédition / paiement demain.
 * Chaque document a un cycle de vie propre : statut (PENDING/VERIFIED/REJECTED),
 * date d'expiration, vérificateur.
 *
 * API exposée : {@code DocumentService} (+ DocumentView et enums OwnerType /
 * DocumentType / DocumentStatus). L'autorisation propre à chaque porteur (ex :
 * OWNER d'un transitaire) reste dans le module appelant, qui délègue ensuite ici.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Document — Pièces justificatives (KYC)"
)
package com.gargotrust.gestion_achats_enligne.document;
