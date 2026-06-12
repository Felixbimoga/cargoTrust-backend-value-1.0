package com.gargotrust.gestion_achats_enligne.iam.admin.controller;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.AssignPermissionsRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreatePermissionRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.PermissionResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.RoleDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.service.IAdminRoleService;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole(" + Role.ALL_SUPER_ROLES_SPEL + ")")
@RequiredArgsConstructor
public class AdminRoleController implements IAdminRoleController {

    private final IAdminRoleService adminRoleService;

    @Override
    public ResponseEntity<List<RoleDetailResponse>> getAllRoles() {
        return ResponseEntity.ok(adminRoleService.getAllRoles());
    }

    @Override
    public ResponseEntity<RoleDetailResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRoleService.createRole(request));
    }

    @Override
    public ResponseEntity<RoleDetailResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(adminRoleService.updateRole(roleId, request));
    }

    @Override
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        adminRoleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RoleDetailResponse> assignPermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(adminRoleService.assignPermissions(roleId, request));
    }

    @Override
    public ResponseEntity<Void> revokePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        adminRoleService.revokePermission(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(adminRoleService.getAllPermissions());
    }

    @Override
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRoleService.createPermission(request));
    }

    @Override
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.ok(adminRoleService.updatePermission(permissionId, request));
    }

    @Override
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        adminRoleService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}
