package com.gargotrust.gestion_achats_enligne.forwarder.repository;

import com.gargotrust.gestion_achats_enligne.forwarder.domain.ForwarderMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ForwarderMemberRepository extends JpaRepository<ForwarderMember, Long> {

    Optional<ForwarderMember> findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);

    /** Tous les membres (OWNER + AGENT) d'une entreprise, pour l'espace équipe. */
    List<ForwarderMember> findByForwarderId(UUID forwarderId);
}
