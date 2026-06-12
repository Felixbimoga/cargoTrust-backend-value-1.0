package com.gargotrust.gestion_achats_enligne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record OtpRequestedEvent(
    UUID accountId,
    String email,
    String otpCode,
    String type,
    Instant requestedAt
) {}
