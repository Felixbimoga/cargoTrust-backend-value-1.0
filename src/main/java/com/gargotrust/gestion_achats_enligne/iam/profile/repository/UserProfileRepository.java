package com.gargotrust.gestion_achats_enligne.iam.profile.repository;

import com.gargotrust.gestion_achats_enligne.iam.profile.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByAccountId(UUID accountId);
    boolean existsByAccountId(UUID accountId);
}
