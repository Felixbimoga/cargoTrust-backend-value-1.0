/**
 * Module Importer — profil des importateurs (rôle CLIENT).
 *
 * Extension métier légère du compte : segment d'activité, habitudes d'importation
 * et besoins logistiques (catégories de produits, modes de transport, origines),
 * adresse de livraison géolocalisable, option Escrow et critères de sélection d'un
 * transitaire. Ces données alimenteront l'algorithme de mise en relation.
 *
 * L'identité personnelle (nom, téléphone WhatsApp, pays, ville) reste dans le profil
 * commun IAM ({@code /api/v1/profile}). Un profil importateur est créé paresseusement
 * à la première écriture (pas de couplage IAM → importer).
 *
 * Dépendances : location (adresse de livraison), catalog (catégories + modes de
 * transport), shared. Aucun accès aux internes d'IAM (compte référencé par UUID).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Importer — Profil importateur (CLIENT)"
)
package com.gargotrust.gestion_achats_enligne.importer;
