package com.gargotrust.gestion_achats_enligne.iam.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean system = true;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    // ── Constantes rôles CargoTrust ────────────────────────────────────────────
    public static final String SUPER_ADMIN       = "ROLE_SUPER_ADMIN";
    public static final String ADMIN_TRANSITAIRE = "ROLE_ADMIN_TRANSITAIRE";
    public static final String TRANSITAIRE       = "ROLE_TRANSITAIRE";
    public static final String CLIENT            = "ROLE_CLIENT";
}
