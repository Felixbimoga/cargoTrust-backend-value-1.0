package com.gargotrust.gestion_achats_enligne.forwarder.dto.request;

import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.DepartureFrequency;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.StructureType;
import com.gargotrust.gestion_achats_enligne.location.AddressCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

/**
 * Mise à jour partielle (PATCH) du transitaire : tout champ null est ignoré.
 * Les collections fournies remplacent intégralement l'ensemble existant.
 */
@Data
@Schema(description = "Mise à jour du profil transitaire (sémantique PATCH : les champs null sont ignorés).")
public class UpdateForwarderRequest {

    // ── Entreprise ──────────────────────────────────────────────────────────────
    @Size(min = 2, max = 200)
    private String legalName;
    private StructureType structureType;
    private LocalDate creationDate;
    private Integer employeeCount;
    @Size(max = 500)
    private String websiteUrl;

    // ── Fiscal & légal ──────────────────────────────────────────────────────────
    @Size(max = 100) private String taxId;                 // NIU / NIF
    @Size(max = 100) private String rccm;
    @Size(max = 100) private String customsLicenseNumber;  // Agrément douane

    // ── Localisation siège (adresse géolocalisable) ─────────────────────────────
    @Schema(description = "Adresse complète du siège (pays/état/ville/quartier + lat/long). Créée ou mise à jour dans le référentiel adresses.")
    private AddressCommand headquartersAddress;

    // ── Origine ─────────────────────────────────────────────────────────────────
    private Boolean hasOriginWarehouse;

    // ── Opérationnel ────────────────────────────────────────────────────────────
    private DepartureFrequency departureFrequency;
    private Boolean variableSchedule;

    // ── Assurance ───────────────────────────────────────────────────────────────
    private Boolean insuranceOffered;
    @Size(max = 200) private String insuranceCoverageRate;
    @Size(max = 200) private String insuranceCompany;

    // ── Digitalisation (équipe à l'origine) ─────────────────────────────────────
    @Size(max = 200) private String originTeamLanguages;
    private Boolean originTeamEquipped;

    // ── Charte ──────────────────────────────────────────────────────────────────
    @Schema(description = "Acceptation de la charte de transparence CargoTrust (irréversible une fois true).")
    private Boolean transparencyCharterAccepted;

    // ── Capacités (remplacement complet de l'ensemble) ──────────────────────────
    private Set<TransportMode> transportModes;

    @Schema(description = "Codes pays d'origine (ISO, ex : CHN, ARE, TUR) issus de /api/v1/geo/countries.")
    private Set<String> originCountryCodes;

    @Schema(description = "Codes pays de destination (ISO) issus de /api/v1/geo/countries.")
    private Set<String> destinationCountryCodes;

    @Schema(description = "Identifiants de catégories de produits (/api/v1/product-categories).")
    private Set<Long> specializationCategoryIds;
}
