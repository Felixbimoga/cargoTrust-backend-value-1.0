package com.gargotrust.gestion_achats_enligne.importer.domain;

import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImportFrequency;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImporterType;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.RefundMethod;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.SelectionCriterion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Profil importateur (rôle CLIENT) : extension métier du compte. Un compte = un profil.
 * Le compte est référencé par UUID ; les capacités/besoins sont queryables (matching).
 */
@Entity
@Table(name = "importer_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ImporterProfile {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)", length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "account_id", nullable = false, unique = true, columnDefinition = "CHAR(36)", length = 36)
    private UUID accountId;

    // ── Nature de l'activité ────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "importer_type", length = 30)
    private ImporterType importerType;

    @Column(name = "business_name", length = 200)
    private String businessName;              // Raison sociale / nom de la boutique (optionnel)

    @Column(name = "tax_id", length = 100)
    private String taxId;                     // Registre / identification fiscale (optionnel, PME)

    // ── Habitudes d'importation ─────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "import_frequency", length = 30)
    private ImportFrequency importFrequency;

    // ── Sécurité financière (Escrow) ────────────────────────────────────────────
    @Column(name = "escrow_enabled", nullable = false)
    @Builder.Default
    private boolean escrowEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", length = 30)
    private RefundMethod refundMethod;

    // ── Localisation de livraison (adresse du module location) ──────────────────
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "delivery_address_id", columnDefinition = "CHAR(36)", length = 36)
    private UUID deliveryAddressId;

    @Column(name = "delivery_country_code", length = 3)
    private String deliveryCountryCode;       // Dénormalisé pour le matching

    // ── Charte d'achat sécurisé ─────────────────────────────────────────────────
    @Column(name = "secured_purchase_charter_accepted", nullable = false)
    @Builder.Default
    private boolean securedPurchaseCharterAccepted = false;

    @Column(name = "charter_accepted_at")
    private Instant charterAcceptedAt;

    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private boolean complete = false;

    // ── Besoins (queryables — socle du matching) ────────────────────────────────
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "importer_categories",
            joinColumns = @JoinColumn(name = "importer_profile_id"))
    @Column(name = "product_category_id")
    @Builder.Default
    private Set<Long> productCategoryIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "importer_transport_modes",
            joinColumns = @JoinColumn(name = "importer_profile_id"))
    @Column(name = "transport_mode", length = 40)
    @Builder.Default
    private Set<TransportMode> transportModes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "importer_origins",
            joinColumns = @JoinColumn(name = "importer_profile_id"))
    @Column(name = "origin_country", length = 3)
    @Builder.Default
    private Set<String> originCountryCodes = new HashSet<>();

    /** Critères de sélection classés par ordre de priorité (1er = plus prioritaire). */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "importer_selection_priorities",
            joinColumns = @JoinColumn(name = "importer_profile_id"))
    @OrderColumn(name = "priority_order")
    @Column(name = "criterion", length = 20)
    @Builder.Default
    private List<SelectionCriterion> selectionPriorities = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
