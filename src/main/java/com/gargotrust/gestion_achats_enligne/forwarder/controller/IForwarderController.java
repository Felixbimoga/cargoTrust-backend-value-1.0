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
import com.gargotrust.gestion_achats_enligne.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Forwarder", description = "Gestion des transitaires (entreprises de transit)")
@SecurityRequirement(name = "bearerAuth")
public interface IForwarderController {

    // ── Espace transitaire ──────────────────────────────────────────────────────

    @Operation(summary = "Créer mon entreprise de transit",
        description = "Réservé au rôle ADMIN_TRANSITAIRE. Le compte devient OWNER du transitaire (statut DRAFT).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "403", description = "ERR_FORWARDER_NOT_A_TRANSITAIRE", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "ERR_FORWARDER_ALREADY_EXISTS", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ForwarderResponse> createMyForwarder(@Valid @RequestBody CreateForwarderRequest request);

    @Operation(summary = "Mon entreprise de transit")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "404", description = "ERR_FORWARDER_NOT_FOUND", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    ResponseEntity<ForwarderResponse> getMyForwarder();

    @Operation(summary = "Mettre à jour mon entreprise", description = "PATCH partiel — réservé à l'OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "403", description = "ERR_FORWARDER_NOT_OWNER", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ForwarderResponse> updateMyForwarder(@Valid @RequestBody UpdateForwarderRequest request);

    @Operation(summary = "Upload du logo", description = "multipart/form-data. JPEG/PNG/WebP, max 5 Mo. OWNER uniquement.")
    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ForwarderResponse> uploadLogo(@RequestParam("file") MultipartFile file);

    @Operation(summary = "Upload de la photo de couverture", description = "multipart/form-data. JPEG/PNG/WebP, max 5 Mo. OWNER uniquement.")
    @PostMapping(value = "/me/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ForwarderResponse> uploadCover(@RequestParam("file") MultipartFile file);

    @Operation(summary = "Ajouter un entrepôt à l'origine", description = "OWNER uniquement.")
    @PostMapping(value = "/me/warehouses", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ForwarderResponse> addWarehouse(@Valid @RequestBody WarehouseRequest request);

    @Operation(summary = "Supprimer un entrepôt", description = "OWNER uniquement.")
    @DeleteMapping("/me/warehouses/{warehouseId}")
    ResponseEntity<ForwarderResponse> removeWarehouse(@PathVariable Long warehouseId);

    @Operation(summary = "Soumettre pour vérification",
        description = "DRAFT/REJECTED → SUBMITTED. Exige les champs légaux minimaux, la charte acceptée, "
                + "et les documents RCCM + agrément douane déjà VERIFIED par l'administration.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "400", description = "ERR_FORWARDER_INCOMPLETE, ERR_FORWARDER_INVALID_STATUS ou ERR_FORWARDER_DOCUMENTS_NOT_VERIFIED", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/me/submit")
    ResponseEntity<ForwarderResponse> submitForVerification();

    // ── Équipe (agents) ───────────────────────────────────────────────────────

    @Operation(summary = "Ajouter un agent à mon entreprise",
        description = "OWNER uniquement. Provisionne un compte TRANSITAIRE actif (mot de passe temporaire "
                + "envoyé par email) et le rattache comme AGENT.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "403", description = "ERR_FORWARDER_NOT_OWNER", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "ERR_ACCOUNT_ALREADY_EXISTS", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/me/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MemberResponse> addAgent(@Valid @RequestBody AddMemberRequest request);

    @Operation(summary = "Lister l'équipe de mon entreprise", description = "OWNER et AGENT confondus. Membre uniquement.")
    @GetMapping("/me/members")
    ResponseEntity<List<MemberResponse>> listMembers();

    @Operation(summary = "Retirer un agent de mon entreprise",
        description = "OWNER uniquement. Détache l'agent (l'OWNER ne peut pas être retiré).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Agent retiré"),
        @ApiResponse(responseCode = "400", description = "ERR_FORWARDER_CANNOT_REMOVE_OWNER", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "ERR_FORWARDER_MEMBER_NOT_FOUND", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/me/members/{memberId}")
    ResponseEntity<Void> removeMember(@PathVariable Long memberId);

    @Operation(summary = "Uploader un document KYC", description = "multipart/form-data. PDF ou image, max 10 Mo. OWNER uniquement.")
    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<DocumentView> uploadDocument(
            @RequestParam("docType") DocumentType docType,
            @RequestParam(value = "expiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            @RequestParam("file") MultipartFile file);

    @Operation(summary = "Lister mes documents KYC")
    @GetMapping("/me/documents")
    ResponseEntity<List<DocumentView>> listMyDocuments();

    @Operation(summary = "Supprimer un de mes documents KYC", description = "OWNER uniquement.")
    @DeleteMapping("/me/documents/{documentId}")
    ResponseEntity<Void> deleteMyDocument(@PathVariable UUID documentId);

    // ── Administration CargoTrust ───────────────────────────────────────────────

    @Operation(summary = "Lister les transitaires (admin)", description = "Filtrable par statut de vérification. Permission forwarders:read.")
    @GetMapping
    ResponseEntity<Page<ForwarderSummaryResponse>> listForwarders(
            @RequestParam(required = false) VerificationStatus status,
            Pageable pageable);

    @Operation(summary = "Détail d'un transitaire (admin)", description = "Permission forwarders:read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "404", description = "ERR_FORWARDER_NOT_FOUND", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{forwarderId}")
    ResponseEntity<ForwarderResponse> getForwarder(@PathVariable UUID forwarderId);

    @Operation(summary = "Valider / rejeter un transitaire (admin)",
        description = "SUBMITTED → VERIFIED ou REJECTED. Permission forwarders:manage.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ForwarderResponse.class))),
        @ApiResponse(responseCode = "400", description = "ERR_FORWARDER_INVALID_STATUS", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/{forwarderId}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ForwarderResponse> verifyForwarder(@PathVariable UUID forwarderId,
                                                      @Valid @RequestBody VerifyForwarderRequest request);
}
