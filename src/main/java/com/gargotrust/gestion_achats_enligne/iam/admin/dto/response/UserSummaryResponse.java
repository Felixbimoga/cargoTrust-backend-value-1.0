package com.gargotrust.gestion_achats_enligne.iam.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UserSummaryResponse {
    private UUID    id;
    private String  email;
    private String  status;
    private String  role;
    private String  firstName;
    private String  lastName;
    private boolean profileComplete;
    private Instant createdAt;
}
