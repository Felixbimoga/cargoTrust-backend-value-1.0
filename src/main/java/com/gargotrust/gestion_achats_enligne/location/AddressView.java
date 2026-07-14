package com.gargotrust.gestion_achats_enligne.location;

import java.math.BigDecimal;
import java.util.UUID;

/** Vue d'une adresse (API exposée du module location). */
public record AddressView(
        UUID       id,
        String     countryCode,
        String     countryName,
        String     stateRegion,
        String     city,
        String     neighborhood,
        String     line,
        BigDecimal latitude,
        BigDecimal longitude,
        String     placeLabel
) {}
