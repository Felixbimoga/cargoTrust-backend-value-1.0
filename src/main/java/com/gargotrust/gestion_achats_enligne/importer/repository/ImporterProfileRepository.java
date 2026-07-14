package com.gargotrust.gestion_achats_enligne.importer.repository;

import com.gargotrust.gestion_achats_enligne.importer.domain.ImporterProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImporterProfileRepository extends JpaRepository<ImporterProfile, UUID> {
    Optional<ImporterProfile> findByAccountId(UUID accountId);
}
