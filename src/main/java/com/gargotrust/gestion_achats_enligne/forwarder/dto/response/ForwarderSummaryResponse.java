package com.gargotrust.gestion_achats_enligne.forwarder.dto.response;

import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/** Vue condensée pour les listings (admin, futur annuaire de matching). */
@Data
@Builder
public class ForwarderSummaryResponse {
    private UUID               id;
    private String             legalName;
    private String             logoUrl;
    private String             headquartersCountryCode;
    private VerificationStatus verificationStatus;
    private Instant            createdAt;
}
