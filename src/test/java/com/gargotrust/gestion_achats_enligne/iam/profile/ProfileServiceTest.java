package com.gargotrust.gestion_achats_enligne.iam.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.profile.domain.UserProfile;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.request.UpdateProfileRequest;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.repository.UserProfileRepository;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.ProfileService;
import com.gargotrust.gestion_achats_enligne.iam.service.StorageService;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileServiceTest {

    @Mock private UserProfileRepository profileRepo;
    @Mock private StorageService        storageService;
    @Mock private CurrentUserContext    currentUser;
    @Spy  private ObjectMapper          objectMapper = new ObjectMapper();

    @InjectMocks
    private ProfileService profileService;

    private UUID        accountId;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        profile   = UserProfile.builder()
                .id(UUID.randomUUID()).accountId(accountId)
                .firstName("John").lastName("Doe")
                .complete(false).build();

        when(currentUser.getAccountId()).thenReturn(accountId);
        when(currentUser.getRole()).thenReturn("ROLE_IMPORTER");
    }

    // ── getMyProfile ──────────────────────────────────────────────────────────

    @Test
    void getMyProfile_ReturnsProfile() {
        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        ProfileResponse resp = profileService.getMyProfile();

        assertEquals(accountId, resp.getAccountId());
        assertEquals("John", resp.getFirstName());
    }

    @Test
    void getMyProfile_NotFound_ThrowsIamException() {
        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.empty());

        IamException ex = assertThrows(IamException.class, () -> profileService.getMyProfile());
        assertEquals(IamException.PROFILE_NOT_FOUND, ex.getErrorCode());
    }

    // ── updateMyProfile ───────────────────────────────────────────────────────

    @Test
    void updateMyProfile_UpdatesFields() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setPhoneNumber("+237600000000");
        req.setRoleMetadata(Map.of("importerType", "INDIVIDUAL"));

        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse resp = profileService.updateMyProfile(req);

        assertEquals("Jane", resp.getFirstName());
        assertEquals("Smith", resp.getLastName());
        assertTrue(resp.isComplete());
    }

    @Test
    void updateMyProfile_NullFields_DoNotOverwrite() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Only Name");

        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse resp = profileService.updateMyProfile(req);

        assertEquals("Only Name", resp.getFirstName());
        assertEquals("Doe", resp.getLastName());
    }

    // ── uploadPhoto ───────────────────────────────────────────────────────────

    @Test
    void uploadPhoto_StoresAndReturnsUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(storageService.store(file, "profiles")).thenReturn("http://localhost/uploads/profiles/photo.jpg");
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse resp = profileService.uploadPhoto(file);

        assertEquals("http://localhost/uploads/profiles/photo.jpg", resp.getProfilePhotoUrl());
        verify(storageService).store(file, "profiles");
    }

    @Test
    void uploadPhoto_DeletesOldPhoto_BeforeStoring() {
        profile.setProfilePhotoUrl("http://localhost/uploads/profiles/old.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "photo", "new.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(storageService.store(file, "profiles")).thenReturn("http://localhost/uploads/profiles/new.jpg");
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        profileService.uploadPhoto(file);

        verify(storageService).delete("http://localhost/uploads/profiles/old.jpg");
        verify(storageService).store(file, "profiles");
    }

    // ── deletePhoto ───────────────────────────────────────────────────────────

    @Test
    void deletePhoto_ClearsUrlAndCallsStorage() {
        profile.setProfilePhotoUrl("http://localhost/uploads/profiles/photo.jpg");

        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        profileService.deletePhoto();

        verify(storageService).delete("http://localhost/uploads/profiles/photo.jpg");
        assertNull(profile.getProfilePhotoUrl());
    }

    @Test
    void deletePhoto_NoPhoto_DoesNothing() {
        when(profileRepo.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        profileService.deletePhoto();

        verify(storageService, never()).delete(any());
        verify(profileRepo, never()).save(any());
    }

    // ── getProfileByAccountId ─────────────────────────────────────────────────

    @Test
    void getProfileByAccountId_ReturnsProfile() {
        UUID otherId = UUID.randomUUID();
        UserProfile other = UserProfile.builder().id(UUID.randomUUID()).accountId(otherId).build();

        when(profileRepo.findByAccountId(otherId)).thenReturn(Optional.of(other));

        ProfileResponse resp = profileService.getProfileByAccountId(otherId);

        assertEquals(otherId, resp.getAccountId());
    }

    @Test
    void getProfileByAccountId_NotFound_ThrowsIamException() {
        UUID otherId = UUID.randomUUID();
        when(profileRepo.findByAccountId(otherId)).thenReturn(Optional.empty());

        IamException ex = assertThrows(IamException.class, () -> profileService.getProfileByAccountId(otherId));
        assertEquals(IamException.PROFILE_NOT_FOUND, ex.getErrorCode());
    }

    // ── createProfileForNewAccount ────────────────────────────────────────────

    @Test
    void createProfileForNewAccount_CreatesWhenAbsent() {
        UUID newId = UUID.randomUUID();
        when(profileRepo.existsByAccountId(newId)).thenReturn(false);

        profileService.createProfileForNewAccount(newId);

        verify(profileRepo).save(argThat(p -> p.getAccountId().equals(newId)));
    }

    @Test
    void createProfileForNewAccount_SkipsWhenAlreadyExists() {
        UUID existingId = UUID.randomUUID();
        when(profileRepo.existsByAccountId(existingId)).thenReturn(true);

        profileService.createProfileForNewAccount(existingId);

        verify(profileRepo, never()).save(any());
    }
}
