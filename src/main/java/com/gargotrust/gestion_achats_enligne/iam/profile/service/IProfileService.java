package com.gargotrust.gestion_achats_enligne.iam.profile.service;

import com.gargotrust.gestion_achats_enligne.iam.profile.dto.request.UpdateProfileRequest;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface IProfileService {
    ProfileResponse getMyProfile();
    ProfileResponse updateMyProfile(UpdateProfileRequest request);
    ProfileResponse uploadPhoto(MultipartFile file);
    void deletePhoto();
    ProfileResponse getProfileByAccountId(UUID accountId);
    void createProfileForNewAccount(UUID accountId);
    void createProfileForNewAccount(UUID accountId, String firstName, String lastName);
}
