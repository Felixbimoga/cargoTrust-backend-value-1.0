package com.gargotrust.gestion_achats_enligne.importer.controller;

import com.gargotrust.gestion_achats_enligne.importer.dto.request.UpdateImporterProfileRequest;
import com.gargotrust.gestion_achats_enligne.importer.dto.response.ImporterProfileResponse;
import com.gargotrust.gestion_achats_enligne.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Importer", description = "Profil importateur (rôle CLIENT)")
@SecurityRequirement(name = "bearerAuth")
public interface IImporterController {

    @Operation(summary = "Mon profil importateur",
        description = "Créé paresseusement si absent (rôle CLIENT requis). L'identité personnelle reste sur /api/v1/profile.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ImporterProfileResponse.class))),
        @ApiResponse(responseCode = "403", description = "ERR_IMPORTER_NOT_A_CLIENT", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    ResponseEntity<ImporterProfileResponse> getMyProfile();

    @Operation(summary = "Mettre à jour mon profil importateur", description = "PATCH partiel — seuls les champs envoyés sont mis à jour.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ImporterProfileResponse.class))),
        @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ImporterProfileResponse> updateMyProfile(@Valid @RequestBody UpdateImporterProfileRequest request);

    @Operation(summary = "Profil importateur par accountId (admin)", description = "Permission users:read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ImporterProfileResponse.class))),
        @ApiResponse(responseCode = "404", description = "ERR_IMPORTER_PROFILE_NOT_FOUND", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{accountId}")
    ResponseEntity<ImporterProfileResponse> getByAccountId(@PathVariable UUID accountId);
}
