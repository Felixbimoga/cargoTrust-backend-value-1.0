package com.gargotrust.gestion_achats_enligne.iam.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class PermissionResponse {
    private Long   id;
    private String name;
    private String resource;
    private String action;
    private String description;
}
