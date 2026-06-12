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
    public static final String IMPORTER          = "ROLE_IMPORTER";
    public static final String AGENT             = "ROLE_AGENT";
    public static final String ADMIN_FORWARDER   = "ROLE_ADMIN_FORWARDER";
    public static final String SUPER_RESPONSIBLE = "ROLE_SUPER_RESPONSIBLE";
    public static final String SUPER_COMMERCIAL  = "ROLE_SUPER_COMMERCIAL";
    public static final String SUPER_FINANCIAL   = "ROLE_SUPER_FINANCIAL";
    public static final String SUPER_PACKAGE     = "ROLE_SUPER_PACKAGE";

    public static final String[] ALL_SUPER_ROLES = {
        SUPER_RESPONSIBLE, SUPER_COMMERCIAL, SUPER_FINANCIAL, SUPER_PACKAGE
    };

    /** SpEL-ready list for use in @PreAuthorize("hasAnyRole(" + Role.ALL_SUPER_ROLES_SPEL + ")") */
    public static final String ALL_SUPER_ROLES_SPEL =
            "'SUPER_RESPONSIBLE','SUPER_COMMERCIAL','SUPER_FINANCIAL','SUPER_PACKAGE'";
}
