/**
 * Module Catalog — référentiels métier administrables par CargoTrust.
 *
 * Contient aujourd'hui les catégories de produits ({@code product_categories}),
 * table gérée par l'admin (et non un enum figé) car le calcul automatique de
 * certains coûts de transport dépend de la catégorie ({@code cost_coefficient}).
 *
 * API exposée : {@code ProductCategoryService} (+ ProductCategoryView) — les autres
 * modules référencent une catégorie par son identifiant.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Catalog — Référentiels métier"
)
package com.gargotrust.gestion_achats_enligne.catalog;
