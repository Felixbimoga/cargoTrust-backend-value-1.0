package com.gargotrust.gestion_achats_enligne.document.service;

import com.gargotrust.gestion_achats_enligne.document.*;
import com.gargotrust.gestion_achats_enligne.document.domain.Document;
import com.gargotrust.gestion_achats_enligne.document.repository.DocumentRepository;
import com.gargotrust.gestion_achats_enligne.shared.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final String DOC_FOLDER = "documents";
    private static final long   DOC_MAX_BYTES = 10 * 1024 * 1024L; // 10 Mo
    private static final Set<String> DOC_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");

    private final DocumentRepository repo;
    private final StorageService     storageService;

    @Override
    @Transactional
    public DocumentView upload(OwnerType ownerType, UUID ownerId, DocumentType docType,
                               LocalDate expiresAt, UUID uploadedBy, MultipartFile file) {
        String url = storageService.store(file, DOC_FOLDER, DOC_TYPES, DOC_MAX_BYTES);
        Document doc = Document.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .docType(docType)
                .fileUrl(url)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .status(DocumentStatus.PENDING)
                .expiresAt(expiresAt)
                .uploadedBy(uploadedBy)
                .build();
        return toView(repo.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentView> listForOwner(OwnerType ownerType, UUID ownerId) {
        return repo.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(ownerType, ownerId)
                .stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentView get(UUID id) {
        return toView(repo.findById(id).orElseThrow(DocumentException::notFound));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Document doc = repo.findById(id).orElseThrow(DocumentException::notFound);
        storageService.delete(doc.getFileUrl());
        repo.delete(doc);
    }

    @Override
    @Transactional
    public DocumentView verify(UUID id, boolean approved, String rejectionReason, UUID verifierAccountId) {
        Document doc = repo.findById(id).orElseThrow(DocumentException::notFound);
        if (approved) {
            doc.setStatus(DocumentStatus.VERIFIED);
            doc.setRejectionReason(null);
        } else {
            doc.setStatus(DocumentStatus.REJECTED);
            doc.setRejectionReason(rejectionReason);
        }
        doc.setVerifiedBy(verifierAccountId);
        doc.setVerifiedAt(Instant.now());
        return toView(repo.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentView> listByStatus(DocumentStatus status, Pageable pageable) {
        return repo.findByStatus(status, pageable).map(this::toView);
    }

    private DocumentView toView(Document d) {
        return new DocumentView(
                d.getId(), d.getOwnerType(), d.getOwnerId(), d.getDocType(),
                d.getFileUrl(), d.getOriginalFilename(), d.getContentType(),
                d.getStatus(), d.getExpiresAt(), d.getRejectionReason(),
                d.getVerifiedBy(), d.getVerifiedAt(), d.getUploadedBy(), d.getUploadedAt());
    }
}
