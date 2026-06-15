package com.gargotrust.gestion_achats_enligne.iam.profile.controller;

import com.gargotrust.gestion_achats_enligne.iam.profile.dto.request.UpdateProfileRequest;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/api/v1/profile",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class ProfileController implements IProfileController {

    private final IProfileService profileService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @Override
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMyProfile(request));
    }

    @Override
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadPhoto(file));
    }

    @Override
    @DeleteMapping("/me/photo")
    public ResponseEntity<Void> deletePhoto() {
        profileService.deletePhoto();
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ProfileResponse> getProfileByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(profileService.getProfileByAccountId(accountId));
    }
}
