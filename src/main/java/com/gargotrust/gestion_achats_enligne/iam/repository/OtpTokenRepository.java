package com.gargotrust.gestion_achats_enligne.iam.repository;

import com.gargotrust.gestion_achats_enligne.iam.domain.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findByAccountIdAndTypeAndConsumedFalseAndExpiresAtAfter(
        UUID accountId, OtpToken.OtpType type, Instant now
    );

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.accountId = :accountId AND o.type = :type")
    void deleteAllByAccountIdAndType(UUID accountId, OtpToken.OtpType type);
}
