package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 150)
    private String displayName;

    @Size(max = 255)
    private String description;
}
