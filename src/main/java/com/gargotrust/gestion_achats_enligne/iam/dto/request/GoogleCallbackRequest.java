package com.gargotrust.gestion_achats_enligne.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Code d'autorisation Google (Web redirect flow)")
public class GoogleCallbackRequest {

    @NotBlank(message = "Le code d'autorisation est requis")
    @Schema(description = "Code d'autorisation retourné par Google après consentement")
    private String code;

    @NotBlank(message = "Le redirectUri est requis")
    @Schema(description = "URI de redirection enregistrée dans Google Console", example = "http://localhost:4200/auth/google/callback")
    private String redirectUri;
}
