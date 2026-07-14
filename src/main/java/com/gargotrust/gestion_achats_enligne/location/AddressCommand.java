package com.gargotrust.gestion_achats_enligne.location;

import java.math.BigDecimal;

/**
 * Commande de création / mise à jour d'une adresse (API exposée du module location).
 * Les libellés pays/état/ville proviennent du référentiel géographique (countriesnow),
 * choisis côté frontend ; latitude/longitude d'un sélecteur cartographique.
 */
public record AddressCommand(
        String     countryCode,   // ISO2 / ISO3 (ex : CM, CMR)
        String     countryName,   // Ex : Cameroon
        String     stateRegion,   // État / région / province
        String     city,
        String     neighborhood,  // Quartier
        String     line,          // Rue, n°, complément
        BigDecimal latitude,
        BigDecimal longitude,
        String     placeLabel     // Libellé libre / résumé affichable
) {}
