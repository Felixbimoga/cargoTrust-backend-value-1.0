package com.gargotrust.gestion_achats_enligne.document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Vue d'un document (API exposée du module document). */
public record DocumentView(
        UUID           id,
        OwnerType      ownerType,
        UUID           ownerId,
        DocumentType   docType,
        String         fileUrl,
        String         originalFilename,
        String         contentType,
        DocumentStatus status,
        LocalDate      expiresAt,
        String         rejectionReason,
        UUID           verifiedBy,
        Instant        verifiedAt,
        UUID           uploadedBy,
        Instant        uploadedAt
) {}
