package com.gargotrust.gestion_achats_enligne.forwarder.domain;

import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Rattachement d'un compte IAM à une entreprise de transit.
 * OWNER = ADMIN_TRANSITAIRE (représentant légal), AGENT = TRANSITAIRE.
 * Le compte est référencé par UUID (frontière de module : pas d'accès direct à IAM).
 */
@Entity
@Table(
    name = "forwarder_members",
    uniqueConstraints = @UniqueConstraint(name = "uq_member_account", columnNames = "account_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ForwarderMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forwarder_id", nullable = false)
    private Forwarder forwarder;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "account_id", nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private MemberRole memberRole;

    @Column(name = "position", length = 150)
    private String position;                      // Fonction : DG, Responsable Logistique, Déclarant…

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;
}
