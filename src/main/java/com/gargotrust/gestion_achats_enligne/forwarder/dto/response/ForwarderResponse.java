package com.gargotrust.gestion_achats_enligne.forwarder.dto.response;

import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.DepartureFrequency;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.StructureType;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import com.gargotrust.gestion_achats_enligne.location.AddressView;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ForwarderResponse {

    private UUID id;

    // Entreprise
    private String        legalName;
    private StructureType structureType;
    private LocalDate     creationDate;
    private Integer       employeeCount;
    private String        logoUrl;
    private String        coverPhotoUrl;
    private String        websiteUrl;

    // Fiscal & légal
    private String        taxId;
    private String        rccm;
    private String        customsLicenseNumber;

    // Localisation
    private AddressView            headquartersAddress;
    private String                 headquartersCountryCode;
    private boolean                hasOriginWarehouse;
    private List<WarehouseResponse> warehouses;

    // Opérationnel
    private DepartureFrequency departureFrequency;
    private boolean            variableSchedule;

    // Assurance
    private boolean insuranceOffered;
    private String  insuranceCoverageRate;
    private String  insuranceCompany;

    // Digitalisation
    private String  originTeamLanguages;
    private boolean originTeamEquipped;

    // Charte
    private boolean transparencyCharterAccepted;
    private Instant charterAcceptedAt;

    // Statut
    private VerificationStatus verificationStatus;
    private String             rejectionReason;

    // Capacités
    private Set<TransportMode>       transportModes;
    private Set<String>              originCountryCodes;
    private Set<String>              destinationCountryCodes;
    private List<ProductCategoryView> specializations;

    private Instant createdAt;
    private Instant updatedAt;
}
