package com.gargotrust.gestion_achats_enligne.shared.events;

import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
    UUID accountId,
    String email,
    String role,
    Instant createdAt
) {}
