package com.gargotrust.gestion_achats_enligne.catalog.repository;

import com.gargotrust.gestion_achats_enligne.catalog.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByCode(String code);
    List<ProductCategory> findByActiveTrue();
}
