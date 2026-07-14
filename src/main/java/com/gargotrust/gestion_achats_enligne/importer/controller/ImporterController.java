package com.gargotrust.gestion_achats_enligne.importer.controller;

import com.gargotrust.gestion_achats_enligne.importer.dto.request.UpdateImporterProfileRequest;
import com.gargotrust.gestion_achats_enligne.importer.dto.response.ImporterProfileResponse;
import com.gargotrust.gestion_achats_enligne.importer.service.IImporterProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/importer", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ImporterController implements IImporterController {

    private final IImporterProfileService importerService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ImporterProfileResponse> getMyProfile() {
        return ResponseEntity.ok(importerService.getMyProfile());
    }

    @Override
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImporterProfileResponse> updateMyProfile(@Valid @RequestBody UpdateImporterProfileRequest request) {
        return ResponseEntity.ok(importerService.updateMyProfile(request));
    }

    @Override
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ImporterProfileResponse> getByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(importerService.getByAccountId(accountId));
    }
}
