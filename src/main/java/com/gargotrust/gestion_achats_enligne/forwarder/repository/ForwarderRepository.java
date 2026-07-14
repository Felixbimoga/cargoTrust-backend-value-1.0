package com.gargotrust.gestion_achats_enligne.forwarder.repository;

import com.gargotrust.gestion_achats_enligne.forwarder.domain.Forwarder;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ForwarderRepository extends JpaRepository<Forwarder, UUID> {

    Page<Forwarder> findByVerificationStatus(VerificationStatus status, Pageable pageable);
}
