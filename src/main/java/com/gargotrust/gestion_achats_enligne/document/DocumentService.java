package com.gargotrust.gestion_achats_enligne.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Gestion des documents KYC — API exposée. Les modules porteurs (forwarder…) vérifient
 * leurs propres droits puis délèguent ; l'administration vérifie/rejette les documents.
 */
public interface DocumentService {

    DocumentView upload(OwnerType ownerType, UUID ownerId, DocumentType docType,
                        LocalDate expiresAt, UUID uploadedBy, MultipartFile file);

    List<DocumentView> listForOwner(OwnerType ownerType, UUID ownerId);

    DocumentView get(UUID id);

    void delete(UUID id);

    DocumentView verify(UUID id, boolean approved, String rejectionReason, UUID verifierAccountId);

    /** File d'attente de vérification (administration). */
    Page<DocumentView> listByStatus(DocumentStatus status, Pageable pageable);
}
