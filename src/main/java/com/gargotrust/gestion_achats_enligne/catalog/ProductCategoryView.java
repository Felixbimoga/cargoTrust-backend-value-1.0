package com.gargotrust.gestion_achats_enligne.catalog;

import java.math.BigDecimal;

/** Vue d'une catégorie de produit (API exposée du module catalog). */
public record ProductCategoryView(
        Long       id,
        String     code,
        String     name,
        String     description,
        BigDecimal costCoefficient,
        boolean    active
) {}
