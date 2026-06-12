package com.gargotrust.gestion_achats_enligne.iam.profile.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Profil utilisateur unifié")
public class ProfileResponse {
    private UUID              id;
    private UUID              accountId;
    private String            firstName;
    private String            lastName;
    private String            phoneNumber;
    private String            country;
    private String            city;
    private String            profilePhotoUrl;
    private String            bio;
    private boolean           complete;
    private Map<String,Object> roleMetadata;
    private Instant           updatedAt;
}
