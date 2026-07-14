package com.gargotrust.gestion_achats_enligne.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Décision de vérification d'un document KYC.")
public class VerifyDocumentRequest {

    @NotNull
    @Schema(description = "true = VERIFIED, false = REJECTED", example = "true")
    private Boolean approved;

    @Size(max = 500)
    @Schema(description = "Motif de rejet (requis si approved = false)")
    private String rejectionReason;
}
