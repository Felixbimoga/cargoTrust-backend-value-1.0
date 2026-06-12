package com.gargotrust.gestion_achats_enligne.iam.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.profile.domain.UserProfile;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.request.UpdateProfileRequest;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.repository.UserProfileRepository;
import com.gargotrust.gestion_achats_enligne.iam.service.StorageService;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements IProfileService {

    private final UserProfileRepository profileRepo;
    private final StorageService        storageService;
    private final CurrentUserContext    currentUser;
    private final ObjectMapper          objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {
        return toResponse(findByCurrentUser());
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(UpdateProfileRequest req) {
        UserProfile p = findByCurrentUser();

        if (req.getFirstName()   != null) p.setFirstName(req.getFirstName());
        if (req.getLastName()    != null) p.setLastName(req.getLastName());
        if (req.getPhoneNumber() != null) p.setPhoneNumber(req.getPhoneNumber());
        if (req.getCountry()     != null) p.setCountry(req.getCountry());
        if (req.getCity()        != null) p.setCity(req.getCity());
        if (req.getBio()         != null) p.setBio(req.getBio());
        if (req.getRoleMetadata() != null) {
            p.setRoleMetadata(toJson(req.getRoleMetadata()));
        }

        p.setComplete(isComplete(p, currentUser.getRole()));
        return toResponse(profileRepo.save(p));
    }

    @Override
    @Transactional
    public ProfileResponse uploadPhoto(MultipartFile file) {
        UserProfile p = findByCurrentUser();
        // Supprime l'ancienne photo
        if (p.getProfilePhotoUrl() != null) {
            storageService.delete(p.getProfilePhotoUrl());
        }
        String url = storageService.store(file, "profiles");
        p.setProfilePhotoUrl(url);
        return toResponse(profileRepo.save(p));
    }

    @Override
    @Transactional
    public void deletePhoto() {
        UserProfile p = findByCurrentUser();
        if (p.getProfilePhotoUrl() != null) {
            storageService.delete(p.getProfilePhotoUrl());
            p.setProfilePhotoUrl(null);
            profileRepo.save(p);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByAccountId(UUID accountId) {
        return toResponse(profileRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IamException(IamException.PROFILE_NOT_FOUND)));
    }

    @Override
    @Transactional
    public void createProfileForNewAccount(UUID accountId) {
        createProfileForNewAccount(accountId, null, null);
    }

    @Override
    @Transactional
    public void createProfileForNewAccount(UUID accountId, String firstName, String lastName) {
        if (!profileRepo.existsByAccountId(accountId)) {
            profileRepo.save(UserProfile.builder()
                    .accountId(accountId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserProfile findByCurrentUser() {
        return profileRepo.findByAccountId(currentUser.getAccountId())
                .orElseThrow(() -> new IamException(IamException.PROFILE_NOT_FOUND));
    }

    private ProfileResponse toResponse(UserProfile p) {
        return ProfileResponse.builder()
                .id(p.getId())
                .accountId(p.getAccountId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .phoneNumber(p.getPhoneNumber())
                .country(p.getCountry())
                .city(p.getCity())
                .profilePhotoUrl(p.getProfilePhotoUrl())
                .bio(p.getBio())
                .complete(p.isComplete())
                .roleMetadata(fromJson(p.getRoleMetadata()))
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private boolean isComplete(UserProfile p, String role) {
        boolean commonOk = p.getFirstName() != null && p.getLastName() != null && p.getPhoneNumber() != null;
        if (!commonOk) return false;
        Map<String,Object> meta = fromJson(p.getRoleMetadata());
        return switch (role) {
            case "ROLE_IMPORTER"       -> meta.containsKey("importerType");
            case "ROLE_AGENT"          -> meta.containsKey("position");
            case "ROLE_ADMIN_FORWARDER"-> meta.containsKey("operationalPosition") && meta.containsKey("forwarderId");
            default                    -> true; // super admins : champs communs suffisent
        };
    }

    private String toJson(Map<String, Object> map) {
        try { return objectMapper.writeValueAsString(map); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (JsonProcessingException e) { return Map.of(); }
    }
}
