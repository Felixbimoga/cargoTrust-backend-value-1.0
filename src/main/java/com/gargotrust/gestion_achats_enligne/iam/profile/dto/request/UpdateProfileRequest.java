package com.gargotrust.gestion_achats_enligne.iam.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Mise à jour du profil utilisateur. roleMetadata contient les champs spécifiques au rôle.")
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Numéro invalide")
    private String phoneNumber;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String city;

    @Size(max = 500)
    private String bio;

    @Schema(description = """
        Champs spécifiques au rôle :
        - ROLE_IMPORTER   → { "importerType": "SME|BEGINNER|ECOMMERCE|LARGE_IMPORTER" }
        - ROLE_AGENT      → { "position": "...", "forwarderId": "uuid" }
        - ROLE_ADMIN_FORWARDER → { "operationalPosition": "...", "forwarderId": "uuid", "additionalPermissions": ["ACCOUNTANT"] }
        - ROLE_SUPER_*    → { "superRole": "DEVELOPER|COMMERCIAL|ACCOUNTANT|SUPER_ADMIN" }
        """,
        example = "{\"importerType\": \"SME\"}")
    private Map<String, Object> roleMetadata;
}
