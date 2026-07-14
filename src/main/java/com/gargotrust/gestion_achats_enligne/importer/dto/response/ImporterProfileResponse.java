package com.gargotrust.gestion_achats_enligne.importer.dto.response;

import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.catalog.TransportMode;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImportFrequency;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.ImporterType;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.RefundMethod;
import com.gargotrust.gestion_achats_enligne.importer.domain.enums.SelectionCriterion;
import com.gargotrust.gestion_achats_enligne.location.AddressView;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ImporterProfileResponse {

    private UUID            id;
    private UUID            accountId;

    private ImporterType    importerType;
    private String          businessName;
    private String          taxId;

    private ImportFrequency importFrequency;

    private boolean         escrowEnabled;
    private RefundMethod    refundMethod;

    private AddressView     deliveryAddress;
    private String          deliveryCountryCode;

    private boolean         securedPurchaseCharterAccepted;
    private Instant         charterAcceptedAt;
    private boolean         complete;

    private List<ProductCategoryView>   categories;
    private Set<TransportMode>          transportModes;
    private Set<String>                 originCountryCodes;
    private List<SelectionCriterion>    selectionPriorities;

    private Instant createdAt;
    private Instant updatedAt;
}
