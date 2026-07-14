package com.gargotrust.gestion_achats_enligne.forwarder.controller;

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
import com.gargotrust.gestion_achats_enligne.forwarder.service.IForwarderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/forwarders", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ForwarderController implements IForwarderController {

    private final IForwarderService forwarderService;

    // ── Espace transitaire ──────────────────────────────────────────────────────

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForwarderResponse> createMyForwarder(@Valid @RequestBody CreateForwarderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forwarderService.createMyForwarder(request));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ForwarderResponse> getMyForwarder() {
        return ResponseEntity.ok(forwarderService.getMyForwarder());
    }

    @Override
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForwarderResponse> updateMyForwarder(@Valid @RequestBody UpdateForwarderRequest request) {
        return ResponseEntity.ok(forwarderService.updateMyForwarder(request));
    }

    @Override
    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ForwarderResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(forwarderService.uploadLogo(file));
    }

    @Override
    @PostMapping(value = "/me/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ForwarderResponse> uploadCover(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(forwarderService.uploadCover(file));
    }

    @Override
    @PostMapping(value = "/me/warehouses", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForwarderResponse> addWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(forwarderService.addWarehouse(request));
    }

    @Override
    @DeleteMapping("/me/warehouses/{warehouseId}")
    public ResponseEntity<ForwarderResponse> removeWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(forwarderService.removeWarehouse(warehouseId));
    }

    @Override
    @PostMapping("/me/submit")
    public ResponseEntity<ForwarderResponse> submitForVerification() {
        return ResponseEntity.ok(forwarderService.submitForVerification());
    }

    // ── Équipe (agents) ───────────────────────────────────────────────────────

    @Override
    @PostMapping(value = "/me/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberResponse> addAgent(@Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(forwarderService.addAgent(request));
    }

    @Override
    @GetMapping("/me/members")
    public ResponseEntity<List<MemberResponse>> listMembers() {
        return ResponseEntity.ok(forwarderService.listMembers());
    }

    @Override
    @DeleteMapping("/me/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long memberId) {
        forwarderService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentView> uploadDocument(
            @RequestParam("docType") DocumentType docType,
            @RequestParam(value = "expiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forwarderService.uploadDocument(docType, expiresAt, file));
    }

    @Override
    @GetMapping("/me/documents")
    public ResponseEntity<List<DocumentView>> listMyDocuments() {
        return ResponseEntity.ok(forwarderService.listMyDocuments());
    }

    @Override
    @DeleteMapping("/me/documents/{documentId}")
    public ResponseEntity<Void> deleteMyDocument(@PathVariable UUID documentId) {
        forwarderService.deleteMyDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    // ── Administration CargoTrust ───────────────────────────────────────────────

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('forwarders:read')")
    public ResponseEntity<Page<ForwarderSummaryResponse>> listForwarders(
            @RequestParam(required = false) VerificationStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(forwarderService.listForwarders(status, pageable));
    }

    @Override
    @GetMapping("/{forwarderId}")
    @PreAuthorize("hasAuthority('forwarders:read')")
    public ResponseEntity<ForwarderResponse> getForwarder(@PathVariable UUID forwarderId) {
        return ResponseEntity.ok(forwarderService.getForwarder(forwarderId));
    }

    @Override
    @PostMapping(value = "/{forwarderId}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('forwarders:manage')")
    public ResponseEntity<ForwarderResponse> verifyForwarder(@PathVariable UUID forwarderId,
                                                             @Valid @RequestBody VerifyForwarderRequest request) {
        return ResponseEntity.ok(forwarderService.verifyForwarder(forwarderId, request));
    }
}
