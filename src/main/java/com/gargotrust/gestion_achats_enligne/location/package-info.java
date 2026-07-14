/**
 * Module Location — géographie et adresses (concern transverse, global à l'app).
 *
 * Deux responsabilités :
 *  - Référentiel géographique : proxy de l'API countriesnow.space (pays / états /
 *    villes) exposé au frontend via {@code /api/v1/geo/**}, avec cache in-memory.
 *    Les zones ne sont donc PAS des enums figés.
 *  - Adresses : table {@code addresses} globale (latitude/longitude pour cartographie),
 *    réutilisable par tout module (forwarder aujourd'hui, importateur demain).
 *
 * API exposée aux autres modules :
 *  - {@code AddressService} (+ AddressCommand / AddressView) — les modules
 *    référencent une adresse par son UUID, jamais par l'entité.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Location — Géographie & Adresses"
)
package com.gargotrust.gestion_achats_enligne.location;
