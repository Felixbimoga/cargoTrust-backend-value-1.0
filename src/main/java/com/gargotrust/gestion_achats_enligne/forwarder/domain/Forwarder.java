package com.gargotrust.gestion_achats_enligne.forwarder.domain;

import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.DepartureFrequency;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.StructureType;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Agrégat racine « entreprise de transit ». Regroupe les données partagées par tous
 * les comptes rattachés (via {@link ForwarderMember}) : identité légale/fiscale,
 * localisation, capacités opérationnelles, assurance, charte et statut de vérification.
 *
 * Les localisations (siège, entrepôts) sont référencées par l'UUID d'une adresse du
 * module {@code location} (jamais par l'entité). Origines/destinations sont des codes
 * pays issus du référentiel géographique ; les spécialisations réfèrent des catégories
 * du module {@code catalog}.
 */
@Entity
@Table(name = "forwarders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Forwarder {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)", length = 36)
    private UUID id;

    // ── Profil entreprise ───────────────────────────────────────────────────────
    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;                     // Raison sociale

    @Enumerated(EnumType.STRING)
    @Column(name = "structure_type", length = 40)
    private StructureType structureType;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_photo_url", length = 500)
    private String coverPhotoUrl;                 // Photo de couverture

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    // ── Fiscal & légal (CEMAC / CEDEAO) ────────────────────────────────────────
    @Column(name = "tax_id", length = 100)
    private String taxId;                         // NIU / NIF

    @Column(name = "rccm", length = 100)
    private String rccm;                          // Registre du Commerce

    @Column(name = "customs_license_number", length = 100)
    private String customsLicenseNumber;          // N° agrément commissionnaire en douane

    // ── Localisation siège (adresse du module location) ─────────────────────────
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "headquarters_address_id", columnDefinition = "CHAR(36)", length = 36)
    private UUID headquartersAddressId;

    /** Code pays du siège dénormalisé (filtre / recherche rapides, sans join sur addresses). */
    @Column(name = "headquarters_country_code", length = 3)
    private String headquartersCountryCode;

    // ── Entrepôts à l'origine (Chine, Dubaï, Turquie…) ──────────────────────────
    @Column(name = "has_origin_warehouse", nullable = false)
    @Builder.Default
    private boolean hasOriginWarehouse = false;

    // ── Opérationnel ────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "departure_frequency", length = 20)
    private DepartureFrequency departureFrequency;

    @Column(name = "variable_schedule", nullable = false)
    @Builder.Default
    private boolean variableSchedule = false;     // Calendrier changeant chaque mois

    // ── Assurance ───────────────────────────────────────────────────────────────
    @Column(name = "insurance_offered", nullable = false)
    @Builder.Default
    private boolean insuranceOffered = false;

    @Column(name = "insurance_coverage_rate", length = 200)
    private String insuranceCoverageRate;         // Ex : au kilo, valeur déclarée, forfaitaire

    @Column(name = "insurance_company", length = 200)
    private String insuranceCompany;

    // ── Digitalisation ──────────────────────────────────────────────────────────
    @Column(name = "origin_team_languages", length = 200)
    private String originTeamLanguages;           // Langues de l'équipe à l'origine (fr / en / zh)

    @Column(name = "origin_team_equipped", nullable = false)
    @Builder.Default
    private boolean originTeamEquipped = false;   // Smartphones récents pour scan/photos/vidéos

    // ── Charte de transparence ──────────────────────────────────────────────────
    @Column(name = "transparency_charter_accepted", nullable = false)
    @Builder.Default
    private boolean transparencyCharterAccepted = false;

    @Column(name = "charter_accepted_at")
    private Instant charterAcceptedAt;

    // ── Cycle de vérification ───────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.DRAFT;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // ── Capacités (queryables — socle du matching) ──────────────────────────────
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "forwarder_transport_modes",
            joinColumns = @JoinColumn(name = "forwarder_id"))
    @Column(name = "transport_mode", length = 40)
    @Builder.Default
    private Set<TransportMode> transportModes = new HashSet<>();

    /** Codes pays d'origine couverts (issus du référentiel géo, ex : CHN, ARE, TUR). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "forwarder_origins",
            joinColumns = @JoinColumn(name = "forwarder_id"))
    @Column(name = "origin_country", length = 3)
    @Builder.Default
    private Set<String> originCountryCodes = new HashSet<>();

    /** Codes pays de destination couverts (corridors Afrique). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "forwarder_destinations",
            joinColumns = @JoinColumn(name = "forwarder_id"))
    @Column(name = "destination_country", length = 3)
    @Builder.Default
    private Set<String> destinationCountryCodes = new HashSet<>();

    /** Identifiants de catégories de produits (module catalog). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "forwarder_specializations",
            joinColumns = @JoinColumn(name = "forwarder_id"))
    @Column(name = "product_category_id")
    @Builder.Default
    private Set<Long> specializationCategoryIds = new HashSet<>();

    // ── Entrepôts (1:N) ─────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "forwarder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ForwarderWarehouse> warehouses = new ArrayList<>();

    // ── Timestamps ──────────────────────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public void addWarehouse(ForwarderWarehouse warehouse) {
        warehouse.setForwarder(this);
        warehouses.add(warehouse);
    }
}
