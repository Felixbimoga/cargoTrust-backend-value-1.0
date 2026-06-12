package com.gargotrust.gestion_achats_enligne.iam.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class RoleDetailResponse {
    private Long         id;
    private String       name;
    private String       displayName;
    private String       description;
    private boolean      system;
    private List<String> permissions;
}
