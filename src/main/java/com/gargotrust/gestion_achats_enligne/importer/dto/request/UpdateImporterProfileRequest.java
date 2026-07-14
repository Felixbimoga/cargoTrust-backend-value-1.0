package com.gargotrust.gestion_achats_enligne.importer.dto.request;

import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImportFrequency;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImporterType;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.RefundMethod;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.SelectionCriterion;
import com.gargotrust.gestion_achats_enligne.location.AddressCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Mise à jour partielle (PATCH) du profil importateur : tout champ null est ignoré.
 * Les collections fournies remplacent intégralement l'ensemble existant.
 */
@Data
@Schema(description = "Mise à jour du profil importateur (PATCH partiel).")
public class UpdateImporterProfileRequest {

    // ── Nature de l'activité ────────────────────────────────────────────────────
    private ImporterType importerType;
    @Size(max = 200) private String businessName;
    @Size(max = 100) private String taxId;

    // ── Habitudes ───────────────────────────────────────────────────────────────
    private ImportFrequency importFrequency;

    // ── Escrow ──────────────────────────────────────────────────────────────────
    private Boolean escrowEnabled;
    private RefundMethod refundMethod;

    // ── Livraison (adresse géolocalisable) ──────────────────────────────────────
    @Schema(description = "Adresse principale de livraison (pays/état/ville/quartier + lat/long).")
    private AddressCommand deliveryAddress;

    // ── Charte d'achat sécurisé ─────────────────────────────────────────────────
    @Schema(description = "Acceptation de la charte d'achat sécurisé (irréversible une fois true).")
    private Boolean securedPurchaseCharterAccepted;

    // ── Besoins (remplacement complet) ──────────────────────────────────────────
    @Schema(description = "Identifiants de catégories de produits (/api/v1/product-categories).")
    private Set<Long> productCategoryIds;

    private Set<TransportMode> transportModes;

    @Schema(description = "Codes pays d'origine (ISO) issus de /api/v1/geo/countries.")
    private Set<String> originCountryCodes;

    @Schema(description = "Critères de sélection classés par priorité (1er = plus prioritaire).")
    private List<SelectionCriterion> selectionPriorities;
}
