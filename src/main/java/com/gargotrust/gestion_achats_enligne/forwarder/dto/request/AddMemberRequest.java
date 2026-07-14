package com.gargotrust.gestion_achats_enligne.forwarder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Ajout d'un agent à mon entreprise de transit. Un compte TRANSITAIRE <b>actif</b>
 * est provisionné et les identifiants (mot de passe temporaire) envoyés par email.
 */
@Data
@Schema(description = "Ajout d'un agent (employé) à l'entreprise de transit.")
public class AddMemberRequest {

    @NotBlank
    @Email
    @Schema(description = "Email de l'agent (identifiant de connexion)", example = "awa.diallo@example.com")
    private String email;

    @Size(max = 100)
    @Schema(description = "Prénom (optionnel, pré-remplit le profil)", example = "Awa")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "Nom (optionnel, pré-remplit le profil)", example = "Diallo")
    private String lastName;

    @Size(max = 150)
    @Schema(description = "Fonction dans l'entreprise", example = "Déclarant en douane")
    private String position;
}
