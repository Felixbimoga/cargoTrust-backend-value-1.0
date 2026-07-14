package com.gargotrust.gestion_achats_enligne.catalog;

import java.util.Collection;
import java.util.List;

/**
 * Gestion des catégories de produits — API exposée. Les lectures / validations sont
 * consommées par les autres modules (ex : spécialisations d'un transitaire) ; les
 * opérations d'écriture sont réservées à l'administration.
 */
public interface ProductCategoryService {

    List<ProductCategoryView> list(boolean onlyActive);

    ProductCategoryView getById(Long id);

    List<ProductCategoryView> getByIds(Collection<Long> ids);

    /** Vérifie que tous les identifiants existent et sont actifs ; lève une exception sinon. */
    void validateActive(Collection<Long> ids);

    ProductCategoryView create(String code, String name, String description, java.math.BigDecimal costCoefficient);

    ProductCategoryView update(Long id, String name, String description, java.math.BigDecimal costCoefficient, Boolean active);

    void delete(Long id);
}
