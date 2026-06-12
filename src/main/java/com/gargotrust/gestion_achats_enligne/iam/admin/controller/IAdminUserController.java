package com.gargotrust.gestion_achats_enligne.iam.admin.controller;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserStatusRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.UserSearchRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@Tag(name = "Admin — Users", description = "User management endpoints (super roles only)")
public interface IAdminUserController {

    @Operation(summary = "Search / list users with filters and pagination")
    @GetMapping
    ResponseEntity<Page<UserSummaryResponse>> searchUsers(@Valid @ModelAttribute UserSearchRequest request);

    @Operation(summary = "Get full user detail including profile")
    @GetMapping("/{accountId}")
    ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable UUID accountId);

    @Operation(summary = "Change user account status (ACTIVE / SUSPENDED)")
    @PatchMapping("/{accountId}/status")
    ResponseEntity<UserDetailResponse> changeStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeUserStatusRequest request);

    @Operation(summary = "Change user role")
    @PatchMapping("/{accountId}/role")
    ResponseEntity<UserDetailResponse> changeRole(
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeUserRoleRequest request);
}
