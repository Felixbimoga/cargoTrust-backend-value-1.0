package com.gargotrust.gestion_achats_enligne.importer.service;

import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryService;
import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.importer.ImporterException;
import com.gargotrust.gestion_achats_enligne.importer.domain.ImporterProfile;
import com.gargotrust.gestion_achats_enligne.importer.dto.request.UpdateImporterProfileRequest;
import com.gargotrust.gestion_achats_enligne.importer.dto.response.ImporterProfileResponse;
import com.gargotrust.gestion_achats_enligne.importer.repository.ImporterProfileRepository;
import com.gargotrust.gestion_achats_enligne.location.AddressService;
import com.gargotrust.gestion_achats_enligne.location.AddressView;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImporterProfileService implements IImporterProfileService {

    private static final String ROLE_CLIENT = "ROLE_CLIENT";

    private final ImporterProfileRepository repo;
    private final CurrentUserContext        currentUser;
    private final AddressService            addressService;
    private final ProductCategoryService    productCategoryService;

    @Override
    @Transactional
    public ImporterProfileResponse getMyProfile() {
        return toResponse(getOrCreateForCurrentClient());
    }

    @Override
    @Transactional
    public ImporterProfileResponse updateMyProfile(UpdateImporterProfileRequest req) {
        ImporterProfile p = getOrCreateForCurrentClient();

        if (req.getImporterType()    != null) p.setImporterType(req.getImporterType());
        if (req.getBusinessName()    != null) p.setBusinessName(req.getBusinessName());
        if (req.getTaxId()           != null) p.setTaxId(req.getTaxId());
        if (req.getImportFrequency() != null) p.setImportFrequency(req.getImportFrequency());

        if (req.getEscrowEnabled()   != null) p.setEscrowEnabled(req.getEscrowEnabled());
        if (req.getRefundMethod()    != null) p.setRefundMethod(req.getRefundMethod());

        // Adresse de livraison : upsert dans le module location
        if (req.getDeliveryAddress() != null) {
            AddressView addr = (p.getDeliveryAddressId() == null)
                    ? addressService.create(req.getDeliveryAddress())
                    : addressService.update(p.getDeliveryAddressId(), req.getDeliveryAddress());
            p.setDeliveryAddressId(addr.id());
            p.setDeliveryCountryCode(addr.countryCode());
        }

        // Charte : irréversible, horodatée à la transition false → true
        if (Boolean.TRUE.equals(req.getSecuredPurchaseCharterAccepted()) && !p.isSecuredPurchaseCharterAccepted()) {
            p.setSecuredPurchaseCharterAccepted(true);
            p.setCharterAcceptedAt(Instant.now());
        }

        if (req.getTransportModes()      != null) p.setTransportModes(new HashSet<>(req.getTransportModes()));
        if (req.getOriginCountryCodes()  != null) p.setOriginCountryCodes(new HashSet<>(req.getOriginCountryCodes()));
        if (req.getSelectionPriorities() != null) p.setSelectionPriorities(new ArrayList<>(req.getSelectionPriorities()));
        if (req.getProductCategoryIds()  != null) {
            productCategoryService.validateActive(req.getProductCategoryIds());
            p.setProductCategoryIds(new HashSet<>(req.getProductCategoryIds()));
        }

        p.setComplete(isComplete(p));
        return toResponse(repo.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public ImporterProfileResponse getByAccountId(UUID accountId) {
        return toResponse(repo.findByAccountId(accountId)
                .orElseThrow(ImporterException::notFound));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ImporterProfile getOrCreateForCurrentClient() {
        if (!currentUser.hasRole(ROLE_CLIENT)) {
            throw ImporterException.notAClient();
        }
        UUID accountId = currentUser.getAccountId();
        return repo.findByAccountId(accountId)
                .orElseGet(() -> repo.save(ImporterProfile.builder().accountId(accountId).build()));
    }

    private boolean isComplete(ImporterProfile p) {
        return p.getImporterType() != null
                && p.getImportFrequency() != null
                && !p.getProductCategoryIds().isEmpty()
                && p.isSecuredPurchaseCharterAccepted();
    }

    private ImporterProfileResponse toResponse(ImporterProfile p) {
        AddressView deliveryAddress = null;
        if (p.getDeliveryAddressId() != null) {
            deliveryAddress = addressService.getMany(List.of(p.getDeliveryAddressId()))
                    .get(p.getDeliveryAddressId());
        }
        List<ProductCategoryView> categories = productCategoryService.getByIds(p.getProductCategoryIds());

        return ImporterProfileResponse.builder()
                .id(p.getId())
                .accountId(p.getAccountId())
                .importerType(p.getImporterType())
                .businessName(p.getBusinessName())
                .taxId(p.getTaxId())
                .importFrequency(p.getImportFrequency())
                .escrowEnabled(p.isEscrowEnabled())
                .refundMethod(p.getRefundMethod())
                .deliveryAddress(deliveryAddress)
                .deliveryCountryCode(p.getDeliveryCountryCode())
                .securedPurchaseCharterAccepted(p.isSecuredPurchaseCharterAccepted())
                .charterAcceptedAt(p.getCharterAcceptedAt())
                .complete(p.isComplete())
                .categories(categories)
                .transportModes(p.getTransportModes())
                .originCountryCodes(p.getOriginCountryCodes())
                .selectionPriorities(p.getSelectionPriorities())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
