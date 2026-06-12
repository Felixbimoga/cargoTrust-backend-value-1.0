package com.gargotrust.gestion_achats_enligne.iam.admin.dto.response;

import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UserDetailResponse {
    private UUID            id;
    private String          email;
    private String          status;
    private String          role;
    private Instant         createdAt;
    private Instant         updatedAt;
    private ProfileResponse profile;
}
