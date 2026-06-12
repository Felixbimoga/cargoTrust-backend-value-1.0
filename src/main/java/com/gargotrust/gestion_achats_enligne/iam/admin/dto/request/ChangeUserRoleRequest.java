package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeUserRoleRequest {
    @NotBlank
    private String roleName;
}
