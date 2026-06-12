package com.gargotrust.gestion_achats_enligne.iam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "ID Token Google (Android ou Web one-tap)")
public class GoogleTokenRequest {

    @NotBlank(message = "L'ID token Google est requis")
    @Schema(description = "ID Token JWT fourni par Google Sign-In", example = "eyJhbGci...")
    private String idToken;
}
