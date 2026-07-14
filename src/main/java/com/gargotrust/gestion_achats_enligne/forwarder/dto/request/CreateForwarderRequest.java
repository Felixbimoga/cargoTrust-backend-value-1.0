package com.gargotrust.gestion_achats_enligne.forwarder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Création de l'entreprise de transit par l'ADMIN_TRANSITAIRE (devient OWNER).")
public class CreateForwarderRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    @Schema(description = "Raison sociale de l'entreprise / du cabinet de transit", example = "Sino-Cargo Logistics SARL")
    private String legalName;

    @Size(max = 150)
    @Schema(description = "Fonction du représentant dans l'entreprise", example = "Directeur Général")
    private String position;
}
