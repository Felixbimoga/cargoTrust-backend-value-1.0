package com.gargotrust.gestion_achats_enligne.document.repository;

import com.gargotrust.gestion_achats_enligne.document.DocumentStatus;
import com.gargotrust.gestion_achats_enligne.document.OwnerType;
import com.gargotrust.gestion_achats_enligne.document.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(OwnerType ownerType, UUID ownerId);

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);
}
