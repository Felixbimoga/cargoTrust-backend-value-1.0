package com.gargotrust.gestion_achats_enligne.iam.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotEmpty
    private List<Long> permissionIds;
}
