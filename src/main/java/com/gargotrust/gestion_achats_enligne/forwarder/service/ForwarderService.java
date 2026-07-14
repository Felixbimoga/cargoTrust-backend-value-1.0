package com.gargotrust.gestion_achats_enligne.forwarder.service;

import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryService;
import com.gargotrust.gestion_achats_enligne.catalog.ProductCategoryView;
import com.gargotrust.gestion_achats_enligne.document.DocumentException;
import com.gargotrust.gestion_achats_enligne.document.DocumentService;
import com.gargotrust.gestion_achats_enligne.document.DocumentStatus;
import com.gargotrust.gestion_achats_enligne.document.DocumentType;
import com.gargotrust.gestion_achats_enligne.document.DocumentView;
import com.gargotrust.gestion_achats_enligne.document.OwnerType;
import com.gargotrust.gestion_achats_enligne.forwarder.ForwarderException;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.Forwarder;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.ForwarderMember;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.ForwarderWarehouse;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.MemberRole;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.AddMemberRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.CreateForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.UpdateForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.VerifyForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.WarehouseRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.ForwarderResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.ForwarderSummaryResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.MemberResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.WarehouseResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.repository.ForwarderMemberRepository;
import com.gargotrust.gestion_achats_enligne.forwarder.repository.ForwarderRepository;
import com.gargotrust.gestion_achats_enligne.location.AddressService;
import com.gargotrust.gestion_achats_enligne.location.AddressView;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import com.gargotrust.gestion_achats_enligne.shared.service.AccountDirectory;
import com.gargotrust.gestion_achats_enligne.shared.service.AccountProvisioningService;
import com.gargotrust.gestion_achats_enligne.shared.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForwarderService implements IForwarderService {

    private static final String ROLE_ADMIN_TRANSITAIRE = "ROLE_ADMIN_TRANSITAIRE";
    /** Rôle IAM attribué aux agents provisionnés (AGENT = TRANSITAIRE). */
    private static final String AGENT_ROLE   = "ROLE_TRANSITAIRE";
    private static final String LOGO_FOLDER  = "forwarders/logos";
    private static final String COVER_FOLDER = "forwarders/covers";

    private final ForwarderRepository        forwarderRepo;
    private final ForwarderMemberRepository  memberRepo;
    private final StorageService             storageService;
    private final CurrentUserContext         currentUser;
    private final AddressService             addressService;
    private final ProductCategoryService     productCategoryService;
    private final DocumentService            documentService;
    private final AccountProvisioningService accountProvisioning;
    private final AccountDirectory           accountDirectory;

    // ── Espace transitaire ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public ForwarderResponse createMyForwarder(CreateForwarderRequest req) {
        if (!currentUser.hasRole(ROLE_ADMIN_TRANSITAIRE)) {
            throw ForwarderException.notATransitaire();
        }
        UUID accountId = currentUser.getAccountId();
        if (memberRepo.existsByAccountId(accountId)) {
            throw ForwarderException.alreadyExists();
        }

        Forwarder forwarder = forwarderRepo.save(Forwarder.builder()
                .legalName(req.getLegalName())
                .verificationStatus(VerificationStatus.DRAFT)
                .build());

        memberRepo.save(ForwarderMember.builder()
                .forwarder(forwarder)
                .accountId(accountId)
                .memberRole(MemberRole.OWNER)
                .position(req.getPosition())
                .build());

        return toResponse(forwarder);
    }

    @Override
    @Transactional(readOnly = true)
    public ForwarderResponse getMyForwarder() {
        return toResponse(requireMyMembership().getForwarder());
    }

    @Override
    @Transactional
    public ForwarderResponse updateMyForwarder(UpdateForwarderRequest req) {
        Forwarder f = requireOwnedForwarder();

        if (req.getLegalName()            != null) f.setLegalName(req.getLegalName());
        if (req.getStructureType()        != null) f.setStructureType(req.getStructureType());
        if (req.getCreationDate()         != null) f.setCreationDate(req.getCreationDate());
        if (req.getEmployeeCount()        != null) f.setEmployeeCount(req.getEmployeeCount());
        if (req.getWebsiteUrl()           != null) f.setWebsiteUrl(req.getWebsiteUrl());

        if (req.getTaxId()                != null) f.setTaxId(req.getTaxId());
        if (req.getRccm()                 != null) f.setRccm(req.getRccm());
        if (req.getCustomsLicenseNumber() != null) f.setCustomsLicenseNumber(req.getCustomsLicenseNumber());

        // Siège : upsert de l'adresse dans le module location
        if (req.getHeadquartersAddress() != null) {
            AddressView hq = (f.getHeadquartersAddressId() == null)
                    ? addressService.create(req.getHeadquartersAddress())
                    : addressService.update(f.getHeadquartersAddressId(), req.getHeadquartersAddress());
            f.setHeadquartersAddressId(hq.id());
            f.setHeadquartersCountryCode(hq.countryCode());
        }

        if (req.getHasOriginWarehouse()   != null) f.setHasOriginWarehouse(req.getHasOriginWarehouse());

        if (req.getDepartureFrequency()   != null) f.setDepartureFrequency(req.getDepartureFrequency());
        if (req.getVariableSchedule()     != null) f.setVariableSchedule(req.getVariableSchedule());

        if (req.getInsuranceOffered()     != null) f.setInsuranceOffered(req.getInsuranceOffered());
        if (req.getInsuranceCoverageRate()!= null) f.setInsuranceCoverageRate(req.getInsuranceCoverageRate());
        if (req.getInsuranceCompany()     != null) f.setInsuranceCompany(req.getInsuranceCompany());

        if (req.getOriginTeamLanguages()  != null) f.setOriginTeamLanguages(req.getOriginTeamLanguages());
        if (req.getOriginTeamEquipped()   != null) f.setOriginTeamEquipped(req.getOriginTeamEquipped());

        // Acceptation de la charte : irréversible, horodatée à la transition false → true
        if (Boolean.TRUE.equals(req.getTransparencyCharterAccepted()) && !f.isTransparencyCharterAccepted()) {
            f.setTransparencyCharterAccepted(true);
            f.setCharterAcceptedAt(Instant.now());
        }

        if (req.getTransportModes()          != null) f.setTransportModes(new HashSet<>(req.getTransportModes()));
        if (req.getOriginCountryCodes()      != null) f.setOriginCountryCodes(new HashSet<>(req.getOriginCountryCodes()));
        if (req.getDestinationCountryCodes() != null) f.setDestinationCountryCodes(new HashSet<>(req.getDestinationCountryCodes()));
        if (req.getSpecializationCategoryIds() != null) {
            productCategoryService.validateActive(req.getSpecializationCategoryIds());
            f.setSpecializationCategoryIds(new HashSet<>(req.getSpecializationCategoryIds()));
        }

        return toResponse(forwarderRepo.save(f));
    }

    @Override
    @Transactional
    public ForwarderResponse uploadLogo(MultipartFile file) {
        Forwarder f = requireOwnedForwarder();
        if (f.getLogoUrl() != null) storageService.delete(f.getLogoUrl());
        f.setLogoUrl(storageService.store(file, LOGO_FOLDER));
        return toResponse(forwarderRepo.save(f));
    }

    @Override
    @Transactional
    public ForwarderResponse uploadCover(MultipartFile file) {
        Forwarder f = requireOwnedForwarder();
        if (f.getCoverPhotoUrl() != null) storageService.delete(f.getCoverPhotoUrl());
        f.setCoverPhotoUrl(storageService.store(file, COVER_FOLDER));
        return toResponse(forwarderRepo.save(f));
    }

    @Override
    @Transactional
    public ForwarderResponse addWarehouse(WarehouseRequest req) {
        Forwarder f = requireOwnedForwarder();
        AddressView address = addressService.create(req.getAddress());
        f.addWarehouse(ForwarderWarehouse.builder()
                .label(req.getLabel())
                .addressId(address.id())
                .build());
        return toResponse(forwarderRepo.save(f));
    }

    @Override
    @Transactional
    public ForwarderResponse removeWarehouse(Long warehouseId) {
        Forwarder f = requireOwnedForwarder();
        ForwarderWarehouse target = f.getWarehouses().stream()
                .filter(w -> w.getId().equals(warehouseId))
                .findFirst()
                .orElseThrow(ForwarderException::warehouseNotFound);
        UUID addressId = target.getAddressId();
        f.getWarehouses().remove(target);
        forwarderRepo.save(f);           // flush : supprime l'entrepôt (orphanRemoval)
        if (addressId != null) addressService.delete(addressId);
        return toResponse(f);
    }

    @Override
    @Transactional
    public ForwarderResponse submitForVerification() {
        Forwarder f = requireOwnedForwarder();
        if (f.getVerificationStatus() != VerificationStatus.DRAFT
                && f.getVerificationStatus() != VerificationStatus.REJECTED) {
            throw ForwarderException.invalidStatus();
        }
        ensureSubmittable(f);
        ensureMandatoryDocumentsVerified(f);
        f.setVerificationStatus(VerificationStatus.SUBMITTED);
        f.setRejectionReason(null);
        return toResponse(forwarderRepo.save(f));
    }

    // ── Équipe (agents) ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public MemberResponse addAgent(AddMemberRequest req) {
        Forwarder f = requireOwnedForwarder();

        // Provisionne un compte TRANSITAIRE actif (mot de passe temporaire envoyé par email).
        // Un email déjà utilisé lève IamException (ERR_ACCOUNT_ALREADY_EXISTS) → 409.
        AccountProvisioningService.ProvisionedAccount account = accountProvisioning.provision(
                new AccountProvisioningService.ProvisionAccountCommand(
                        req.getEmail(), AGENT_ROLE, req.getFirstName(), req.getLastName()));

        ForwarderMember member = memberRepo.save(ForwarderMember.builder()
                .forwarder(f)
                .accountId(account.accountId())
                .memberRole(MemberRole.AGENT)
                .position(req.getPosition())
                .build());

        return toMemberResponses(List.of(member)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers() {
        Forwarder f = requireMyMembership().getForwarder();
        return toMemberResponses(memberRepo.findByForwarderId(f.getId()));
    }

    @Override
    @Transactional
    public void removeMember(Long memberId) {
        Forwarder f = requireOwnedForwarder();
        ForwarderMember member = memberRepo.findById(memberId)
                .orElseThrow(ForwarderException::memberNotFound);
        if (!member.getForwarder().getId().equals(f.getId())) {
            throw ForwarderException.memberNotFound();   // n'appartient pas à mon entreprise
        }
        if (member.getMemberRole() == MemberRole.OWNER) {
            throw ForwarderException.cannotRemoveOwner();
        }
        memberRepo.delete(member);
    }

    // ── Documents KYC ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DocumentView uploadDocument(DocumentType docType, LocalDate expiresAt, MultipartFile file) {
        Forwarder f = requireOwnedForwarder();
        return documentService.upload(OwnerType.FORWARDER, f.getId(), docType, expiresAt,
                currentUser.getAccountId(), file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentView> listMyDocuments() {
        Forwarder f = requireMyMembership().getForwarder();
        return documentService.listForOwner(OwnerType.FORWARDER, f.getId());
    }

    @Override
    @Transactional
    public void deleteMyDocument(UUID documentId) {
        Forwarder f = requireOwnedForwarder();
        DocumentView doc = documentService.get(documentId);
        if (doc.ownerType() != OwnerType.FORWARDER || !doc.ownerId().equals(f.getId())) {
            throw DocumentException.notFound();   // n'appartient pas à mon entreprise
        }
        documentService.delete(documentId);
    }

    // ── Administration ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ForwarderSummaryResponse> listForwarders(VerificationStatus status, Pageable pageable) {
        Page<Forwarder> page = (status == null)
                ? forwarderRepo.findAll(pageable)
                : forwarderRepo.findByVerificationStatus(status, pageable);
        return page.map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ForwarderResponse getForwarder(UUID forwarderId) {
        return toResponse(forwarderRepo.findById(forwarderId)
                .orElseThrow(ForwarderException::notFound));
    }

    @Override
    @Transactional
    public ForwarderResponse verifyForwarder(UUID forwarderId, VerifyForwarderRequest req) {
        Forwarder f = forwarderRepo.findById(forwarderId).orElseThrow(ForwarderException::notFound);
        if (f.getVerificationStatus() != VerificationStatus.SUBMITTED) {
            throw ForwarderException.invalidStatus();
        }
        if (Boolean.TRUE.equals(req.getApproved())) {
            f.setVerificationStatus(VerificationStatus.VERIFIED);
            f.setRejectionReason(null);
        } else {
            f.setVerificationStatus(VerificationStatus.REJECTED);
            f.setRejectionReason(req.getRejectionReason());
        }
        return toResponse(forwarderRepo.save(f));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ForwarderMember requireMyMembership() {
        return memberRepo.findByAccountId(currentUser.getAccountId())
                .orElseThrow(ForwarderException::notFound);
    }

    /** Le compte courant doit être OWNER de son entreprise pour la modifier. */
    private Forwarder requireOwnedForwarder() {
        ForwarderMember member = requireMyMembership();
        if (member.getMemberRole() != MemberRole.OWNER) {
            throw ForwarderException.notOwner();
        }
        return member.getForwarder();
    }

    /** Champs minimaux exigés avant soumission (filtre l'informel). */
    private void ensureSubmittable(Forwarder f) {
        boolean ok = isFilled(f.getLegalName())
                && f.getStructureType() != null
                && isFilled(f.getTaxId())
                && isFilled(f.getRccm())
                && isFilled(f.getCustomsLicenseNumber())
                && f.getHeadquartersAddressId() != null
                && f.isTransparencyCharterAccepted()
                && !f.getTransportModes().isEmpty()
                && !f.getDestinationCountryCodes().isEmpty();
        if (!ok) throw ForwarderException.incomplete();
    }

    /**
     * Exige que les pièces légales déterminantes (RCCM + agrément de commissionnaire
     * en douane) aient été déposées ET validées par l'administration avant soumission.
     * Couplage KYC ↔ soumission : filtre l'informel en amont de la file de validation.
     */
    private void ensureMandatoryDocumentsVerified(Forwarder f) {
        List<DocumentView> docs = documentService.listForOwner(OwnerType.FORWARDER, f.getId());
        boolean rccmVerified = hasVerified(docs, DocumentType.RCCM);
        boolean licenseVerified = hasVerified(docs, DocumentType.CUSTOMS_LICENSE);
        if (!(rccmVerified && licenseVerified)) {
            throw ForwarderException.documentsNotVerified();
        }
    }

    private boolean hasVerified(List<DocumentView> docs, DocumentType type) {
        return docs.stream().anyMatch(d -> d.docType() == type && d.status() == DocumentStatus.VERIFIED);
    }

    private boolean isFilled(String s) { return s != null && !s.isBlank(); }

    private ForwarderResponse toResponse(Forwarder f) {
        // Résolution en lot des adresses (siège + entrepôts) pour éviter le N+1
        List<UUID> addressIds = new ArrayList<>();
        if (f.getHeadquartersAddressId() != null) addressIds.add(f.getHeadquartersAddressId());
        f.getWarehouses().forEach(w -> { if (w.getAddressId() != null) addressIds.add(w.getAddressId()); });
        Map<UUID, AddressView> addresses = addressService.getMany(addressIds);

        List<WarehouseResponse> warehouses = f.getWarehouses().stream()
                .map(w -> WarehouseResponse.builder()
                        .id(w.getId())
                        .label(w.getLabel())
                        .address(w.getAddressId() != null ? addresses.get(w.getAddressId()) : null)
                        .build())
                .toList();

        List<ProductCategoryView> specializations =
                productCategoryService.getByIds(f.getSpecializationCategoryIds());

        return ForwarderResponse.builder()
                .id(f.getId())
                .legalName(f.getLegalName())
                .structureType(f.getStructureType())
                .creationDate(f.getCreationDate())
                .employeeCount(f.getEmployeeCount())
                .logoUrl(f.getLogoUrl())
                .coverPhotoUrl(f.getCoverPhotoUrl())
                .websiteUrl(f.getWebsiteUrl())
                .taxId(f.getTaxId())
                .rccm(f.getRccm())
                .customsLicenseNumber(f.getCustomsLicenseNumber())
                .headquartersAddress(f.getHeadquartersAddressId() != null ? addresses.get(f.getHeadquartersAddressId()) : null)
                .headquartersCountryCode(f.getHeadquartersCountryCode())
                .hasOriginWarehouse(f.isHasOriginWarehouse())
                .warehouses(warehouses)
                .departureFrequency(f.getDepartureFrequency())
                .variableSchedule(f.isVariableSchedule())
                .insuranceOffered(f.isInsuranceOffered())
                .insuranceCoverageRate(f.getInsuranceCoverageRate())
                .insuranceCompany(f.getInsuranceCompany())
                .originTeamLanguages(f.getOriginTeamLanguages())
                .originTeamEquipped(f.isOriginTeamEquipped())
                .transparencyCharterAccepted(f.isTransparencyCharterAccepted())
                .charterAcceptedAt(f.getCharterAcceptedAt())
                .verificationStatus(f.getVerificationStatus())
                .rejectionReason(f.getRejectionReason())
                .transportModes(f.getTransportModes())
                .originCountryCodes(f.getOriginCountryCodes())
                .destinationCountryCodes(f.getDestinationCountryCodes())
                .specializations(specializations)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    /**
     * Mappe des membres en réponses enrichies : résout email + nom via l'annuaire des
     * comptes (module IAM) en une seule requête en lot pour éviter le N+1.
     */
    private List<MemberResponse> toMemberResponses(List<ForwarderMember> members) {
        List<UUID> accountIds = members.stream().map(ForwarderMember::getAccountId).toList();
        Map<UUID, AccountDirectory.AccountSummary> accounts = accountDirectory.findAll(accountIds);

        return members.stream()
                .map(m -> {
                    AccountDirectory.AccountSummary a = accounts.get(m.getAccountId());
                    return MemberResponse.builder()
                            .id(m.getId())
                            .accountId(m.getAccountId())
                            .email(a != null ? a.email() : null)
                            .firstName(a != null ? a.firstName() : null)
                            .lastName(a != null ? a.lastName() : null)
                            .memberRole(m.getMemberRole())
                            .position(m.getPosition())
                            .joinedAt(m.getJoinedAt())
                            .build();
                })
                .toList();
    }

    private ForwarderSummaryResponse toSummary(Forwarder f) {
        return ForwarderSummaryResponse.builder()
                .id(f.getId())
                .legalName(f.getLegalName())
                .logoUrl(f.getLogoUrl())
                .headquartersCountryCode(f.getHeadquartersCountryCode())
                .verificationStatus(f.getVerificationStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
