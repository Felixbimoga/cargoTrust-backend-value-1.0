package com.gargotrust.gestion_achats_enligne.iam.profile.controller;

import com.gargotrust.gestion_achats_enligne.iam.profile.dto.request.UpdateProfileRequest;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import com.gargotrust.gestion_achats_enligne.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Profile", description = "Gestion du profil utilisateur")
@SecurityRequirement(name = "bearerAuth")
public interface IProfileController {

    @Operation(summary = "Mon profil")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "404", description = "ERR_PROFILE_NOT_FOUND", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    ResponseEntity<ProfileResponse> getMyProfile();

    @Operation(summary = "Mettre à jour mon profil", description = "PATCH partiel — seuls les champs envoyés sont mis à jour.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/me")
    ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request);

    @Operation(summary = "Upload photo de profil", description = "multipart/form-data. JPEG/PNG/WebP, max 5 Mo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "ERR_PROFILE_PHOTO_INVALID ou ERR_PROFILE_PHOTO_TOO_LARGE",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(value = "/me/photo", consumes = "multipart/form-data")
    ResponseEntity<ProfileResponse> uploadPhoto(@RequestParam("file") MultipartFile file);

    @Operation(summary = "Supprimer la photo de profil")
    @DeleteMapping("/me/photo")
    ResponseEntity<Void> deletePhoto();

    @Operation(summary = "Profil par accountId (Super Admin)", description = "Réservé aux rôles SUPER_*")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{accountId}")
    ResponseEntity<ProfileResponse> getProfileByAccountId(@PathVariable UUID accountId);
}
