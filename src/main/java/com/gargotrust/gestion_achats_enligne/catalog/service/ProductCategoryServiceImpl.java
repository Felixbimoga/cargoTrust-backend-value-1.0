package com.gargotrust.gestion_achats_enligne.catalog.service;

import com.gargotrust.gestion_achats_enligne.catalog.CatalogException;
import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryService;
import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.catalog.domain.ProductCategory;
import com.gargotrust.gestion_achats_enligne.catalog.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryView> list(boolean onlyActive) {
        List<ProductCategory> categories = onlyActive ? repo.findByActiveTrue() : repo.findAll();
        return categories.stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryView getById(Long id) {
        return toView(repo.findById(id).orElseThrow(CatalogException::notFound));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryView> getByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repo.findAllById(ids).stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActive(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<Long> wanted = new HashSet<>(ids);
        long activeMatches = repo.findAllById(wanted).stream()
                .filter(ProductCategory::isActive)
                .count();
        if (activeMatches != wanted.size()) {
            throw CatalogException.invalid();
        }
    }

    @Override
    @Transactional
    public ProductCategoryView create(String code, String name, String description, BigDecimal costCoefficient) {
        if (repo.existsByCode(code)) throw CatalogException.codeExists();
        ProductCategory c = ProductCategory.builder()
                .code(code)
                .name(name)
                .description(description)
                .costCoefficient(costCoefficient != null ? costCoefficient : BigDecimal.ONE)
                .active(true)
                .build();
        return toView(repo.save(c));
    }

    @Override
    @Transactional
    public ProductCategoryView update(Long id, String name, String description, BigDecimal costCoefficient, Boolean active) {
        ProductCategory c = repo.findById(id).orElseThrow(CatalogException::notFound);
        if (name != null)            c.setName(name);
        if (description != null)     c.setDescription(description);
        if (costCoefficient != null) c.setCostCoefficient(costCoefficient);
        if (active != null)          c.setActive(active);
        return toView(repo.save(c));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductCategory c = repo.findById(id).orElseThrow(CatalogException::notFound);
        // Suppression logique : préserve l'intégrité si des transitaires y référent déjà.
        c.setActive(false);
        repo.save(c);
    }

    private ProductCategoryView toView(ProductCategory c) {
        return new ProductCategoryView(c.getId(), c.getCode(), c.getName(),
                c.getDescription(), c.getCostCoefficient(), c.isActive());
    }
}
