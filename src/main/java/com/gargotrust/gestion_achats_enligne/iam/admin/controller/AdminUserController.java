package com.gargotrust.gestion_achats_enligne.iam.admin.controller;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserStatusRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateUserRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.UserSearchRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserSummaryResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.service.IAdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/api/v1/admin/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
//@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController implements IAdminUserController {

    private final IAdminUserService adminUserService;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_TRANSITAIRE', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserSummaryResponse>> searchUsers(@Valid @ModelAttribute UserSearchRequest request) {
        return ResponseEntity.ok(adminUserService.searchUsers(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_TRANSITAIRE')")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable UUID accountId) {
        return ResponseEntity.ok(adminUserService.getUserDetail(accountId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_TRANSITAIRE', 'SUPER_ADMIN')")
    public ResponseEntity<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(request));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDetailResponse> changeStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeUserStatusRequest request) {
        return ResponseEntity.ok(adminUserService.changeStatus(accountId, request));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDetailResponse> changeRole(
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        return ResponseEntity.ok(adminUserService.changeRole(accountId, request));
    }
}
