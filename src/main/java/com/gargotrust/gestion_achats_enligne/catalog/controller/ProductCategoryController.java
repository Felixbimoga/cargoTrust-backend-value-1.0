package com.gargotrust.gestion_achats_enligne.catalog.controller;

import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryService;
import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.catalog.dto.request.CreateProductCategoryRequest;
import com.gargotrust.gestion_achats_enligne.catalog.dto.request.UpdateProductCategoryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Catégories de produits transportables")
public class ProductCategoryController {

    private final ProductCategoryService service;

    // ── Lecture publique (formulaires) ──────────────────────────────────────────

    @Operation(summary = "Lister les catégories", description = "Par défaut, seules les catégories actives.")
    @GetMapping(value = "/api/v1/product-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductCategoryView>> list(
            @RequestParam(defaultValue = "true") boolean onlyActive) {
        return ResponseEntity.ok(service.list(onlyActive));
    }

    @Operation(summary = "Détail d'une catégorie")
    @GetMapping(value = "/api/v1/product-categories/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductCategoryView> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ── Administration ──────────────────────────────────────────────────────────

    @Operation(summary = "Créer une catégorie (admin)")
    @PostMapping(value = "/api/v1/admin/product-categories",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('catalog:manage')")
    public ResponseEntity<ProductCategoryView> create(@Valid @RequestBody CreateProductCategoryRequest req) {
        ProductCategoryView created = service.create(req.getCode(), req.getName(), req.getDescription(), req.getCostCoefficient());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Mettre à jour une catégorie (admin)")
    @PatchMapping(value = "/api/v1/admin/product-categories/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('catalog:manage')")
    public ResponseEntity<ProductCategoryView> update(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateProductCategoryRequest req) {
        return ResponseEntity.ok(service.update(id, req.getName(), req.getDescription(), req.getCostCoefficient(), req.getActive()));
    }

    @Operation(summary = "Désactiver une catégorie (admin)", description = "Suppression logique.")
    @DeleteMapping("/api/v1/admin/product-categories/{id}")
    @PreAuthorize("hasAuthority('catalog:manage')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
