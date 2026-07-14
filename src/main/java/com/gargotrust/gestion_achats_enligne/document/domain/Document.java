package com.gargotrust.gestion_achats_enligne.document.domain;

import com.gargotrust.gestion_achats_enligne.document.DocumentStatus;
import com.gargotrust.gestion_achats_enligne.document.DocumentType;
import com.gargotrust.gestion_achats_enligne.document.OwnerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pièce justificative polymorphe, rattachée à un porteur par {@code ownerType + ownerId}.
 */
@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_owner",  columnList = "owner_type, owner_id"),
    @Index(name = "idx_document_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Document {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private OwnerType ownerType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "owner_id", nullable = false, columnDefinition = "CHAR(36)", length = 36)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 40)
    private DocumentType docType;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "verified_by", columnDefinition = "CHAR(36)", length = 36)
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uploaded_by", columnDefinition = "CHAR(36)", length = 36)
    private UUID uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() { uploadedAt = Instant.now(); }
}
