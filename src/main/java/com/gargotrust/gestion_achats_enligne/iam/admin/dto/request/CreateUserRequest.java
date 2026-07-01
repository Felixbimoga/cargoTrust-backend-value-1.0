package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    @Email
    @Schema(description = "Email du nouvel utilisateur (sert d'identifiant de connexion)",
            example = "jean.dupont@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Rôle à attribuer, avec ou sans préfixe ROLE_",
            example = "TRANSITAIRE",
            allowableValues = {"SUPER_ADMIN", "ADMIN_TRANSITAIRE", "TRANSITAIRE", "CLIENT"})
    private String roleName;

    @Schema(description = "Prénom (optionnel, pré-remplit le profil)", example = "Jean")
    private String firstName;

    @Schema(description = "Nom (optionnel, pré-remplit le profil)", example = "Dupont")
    private String lastName;
}
