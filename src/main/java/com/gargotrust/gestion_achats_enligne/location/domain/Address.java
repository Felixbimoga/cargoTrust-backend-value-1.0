package com.gargotrust.gestion_achats_enligne.location.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Adresse physique globale (siège transitaire, entrepôt, livraison importateur…).
 * Porte latitude/longitude pour l'affichage cartographique.
 */
@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)", length = 36)
    private UUID id;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "country_name", length = 120)
    private String countryName;

    @Column(name = "state_region", length = 150)
    private String stateRegion;

    @Column(length = 150)
    private String city;

    @Column(length = 150)
    private String neighborhood;

    @Column(length = 300)
    private String line;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "place_label", length = 255)
    private String placeLabel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
