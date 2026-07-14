package com.gargotrust.gestion_achats_enligne.document.controller;

import com.gargotrust.gestion_achats_enligne.document.DocumentService;
import com.gargotrust.gestion_achats_enligne.document.DocumentStatus;
import com.gargotrust.gestion_achats_enligne.document.DocumentView;
import com.gargotrust.gestion_achats_enligne.document.dto.VerifyDocumentRequest;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/admin/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Admin Documents", description = "Vérification des pièces justificatives KYC")
@SecurityRequirement(name = "bearerAuth")
public class AdminDocumentController {

    private final DocumentService    documentService;
    private final CurrentUserContext currentUser;

    @Operation(summary = "File d'attente des documents à vérifier", description = "Permission forwarders:read.")
    @GetMapping
    @PreAuthorize("hasAuthority('forwarders:read')")
    public ResponseEntity<Page<DocumentView>> list(
            @RequestParam(defaultValue = "PENDING") DocumentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(documentService.listByStatus(status, pageable));
    }

    @Operation(summary = "Détail d'un document", description = "Permission forwarders:read.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('forwarders:read')")
    public ResponseEntity<DocumentView> get(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.get(id));
    }

    @Operation(summary = "Valider / rejeter un document", description = "Permission forwarders:manage.")
    @PostMapping(value = "/{id}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('forwarders:manage')")
    public ResponseEntity<DocumentView> verify(@PathVariable UUID id,
                                               @Valid @RequestBody VerifyDocumentRequest req) {
        return ResponseEntity.ok(documentService.verify(
                id, Boolean.TRUE.equals(req.getApproved()), req.getRejectionReason(), currentUser.getAccountId()));
    }
}
