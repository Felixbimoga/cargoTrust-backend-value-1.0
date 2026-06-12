package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeUserStatusRequest {
    @NotBlank
    @Pattern(regexp = "ACTIVE|SUSPENDED", message = "Valeurs acceptées : ACTIVE, SUSPENDED")
    private String status;

    private String reason;
}
