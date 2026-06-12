package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserSearchRequest {

    @Schema(description = "Filtre sur l'email (recherche partielle)")
    private String email;

    @Schema(description = "Filtre sur le statut", allowableValues = {"ACTIVE", "SUSPENDED", "PENDING_VERIFICATION"})
    private String status;

    @Schema(description = "Filtre sur le rôle (ex: ROLE_IMPORTER)")
    private String role;

    @Min(value = 0, message = "La page doit être >= 0")
    @Schema(description = "Numéro de page (0-based)", defaultValue = "0", example = "0")
    private int page = 0;

    @Min(value = 1, message = "La taille de page doit être >= 1")
    @Max(value = 100, message = "La taille de page doit être <= 100")
    @Schema(description = "Taille de page", defaultValue = "20", example = "20")
    private int size = 20;
}
