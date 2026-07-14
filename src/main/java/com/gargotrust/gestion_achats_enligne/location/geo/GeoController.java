package com.gargotrust.gestion_achats_enligne.location.geo;

import com.gargotrust.gestion_achats_enligne.location.geo.GeoDtos.CountryDto;
import com.gargotrust.gestion_achats_enligne.location.geo.GeoDtos.CountryStatesDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Référentiel géographique pour les formulaires (pays → état/région → ville).
 * Données publiques non sensibles ; alimente les listes déroulantes du frontend.
 */
@RestController
@RequestMapping(value = "/api/v1/geo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Geo", description = "Référentiel géographique (pays / états / villes)")
public class GeoController {

    private final GeoService geoService;

    @Operation(summary = "Lister les pays", description = "Nom + codes ISO2/ISO3.")
    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> countries() {
        return ResponseEntity.ok(geoService.getCountries());
    }

    @Operation(summary = "Lister les états / régions d'un pays")
    @GetMapping("/states")
    public ResponseEntity<CountryStatesDto> states(@RequestParam String country) {
        return ResponseEntity.ok(geoService.getStates(country));
    }

    @Operation(summary = "Lister les villes",
        description = "D'un état si 'state' est fourni, sinon toutes les villes du pays.")
    @GetMapping("/cities")
    public ResponseEntity<List<String>> cities(@RequestParam String country,
                                               @RequestParam(required = false) String state) {
        return ResponseEntity.ok(geoService.getCities(country, state));
    }
}
