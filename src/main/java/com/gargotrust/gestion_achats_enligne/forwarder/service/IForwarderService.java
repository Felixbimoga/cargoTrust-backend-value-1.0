package com.gargotrust.gestion_achats_enligne.forwarder.service;

import com.gargotrust.gestion_achats_enligne.document.DocumentType;
import com.gargotrust.gestion_achats_enligne.document.DocumentView;
import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.VerificationStatus;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.AddMemberRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.CreateForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.UpdateForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.VerifyForwarderRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.request.WarehouseRequest;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.ForwarderResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.ForwarderSummaryResponse;
import com.gargotrust.gestion_achats_enligne.forwarder.dto.response.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IForwarderService {

    // ── Espace transitaire (compte courant) ────────────────────────────────────
    ForwarderResponse createMyForwarder(CreateForwarderRequest request);
    ForwarderResponse getMyForwarder();
    ForwarderResponse updateMyForwarder(UpdateForwarderRequest request);
    ForwarderResponse uploadLogo(MultipartFile file);
    ForwarderResponse uploadCover(MultipartFile file);
    ForwarderResponse addWarehouse(WarehouseRequest request);
    ForwarderResponse removeWarehouse(Long warehouseId);
    ForwarderResponse submitForVerification();

    // ── Équipe (agents rattachés à mon entreprise) ─────────────────────────────
    MemberResponse addAgent(AddMemberRequest request);
    List<MemberResponse> listMembers();
    void removeMember(Long memberId);

    // ── Documents KYC (rattachés à mon entreprise) ─────────────────────────────
    DocumentView uploadDocument(DocumentType docType, LocalDate expiresAt, MultipartFile file);
    List<DocumentView> listMyDocuments();
    void deleteMyDocument(UUID documentId);

    // ── Espace administration CargoTrust ───────────────────────────────────────
    Page<ForwarderSummaryResponse> listForwarders(VerificationStatus status, Pageable pageable);
    ForwarderResponse getForwarder(UUID forwarderId);
    ForwarderResponse verifyForwarder(UUID forwarderId, VerifyForwarderRequest request);
}
