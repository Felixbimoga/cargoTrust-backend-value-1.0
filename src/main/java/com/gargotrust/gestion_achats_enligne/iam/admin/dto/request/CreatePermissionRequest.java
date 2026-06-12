package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank @Size(max = 50)
    private String resource;

    @NotBlank @Size(max = 50)
    private String action;

    @Size(max = 255)
    private String description;
}
