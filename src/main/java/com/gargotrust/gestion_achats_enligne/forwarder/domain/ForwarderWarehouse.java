package com.gargotrust.gestion_achats_enligne.forwarder.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entrepôt / dépôt du transitaire à l'origine (Chine, Dubaï, Turquie, etc.) — adresse
 * d'envoi des colis des importateurs. La localisation précise (pays/ville/quartier +
 * lat/long) est portée par une adresse du module {@code location}, référencée par UUID.
 */
@Entity
@Table(name = "forwarder_warehouses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ForwarderWarehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forwarder_id", nullable = false)
    private Forwarder forwarder;

    @Column(name = "label", length = 150)
    private String label;                         // Ex : « Dépôt Guangzhou »

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "address_id", columnDefinition = "CHAR(36)", length = 36)
    private UUID addressId;
}
