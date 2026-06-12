package com.gargotrust.gestion_achats_enligne.iam.profile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfile {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "account_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private UUID accountId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private boolean complete = false;

    // JSON stocké en String, désérialisé par ProfileService via ObjectMapper.
    // Nouveau rôle = nouveau DTO côté service, zéro migration SQL.
    @Column(name = "role_metadata", columnDefinition = "JSON")
    private String roleMetadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
