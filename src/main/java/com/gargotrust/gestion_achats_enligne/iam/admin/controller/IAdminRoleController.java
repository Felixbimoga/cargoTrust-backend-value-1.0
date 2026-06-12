package com.gargotrust.gestion_achats_enligne.iam.admin.controller;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.AssignPermissionsRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreatePermissionRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.PermissionResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.RoleDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin — Roles & Permissions", description = "RBAC management endpoints (super roles only)")
public interface IAdminRoleController {

    // ── Roles ─────────────────────────────────────────────────────────────────

    @Operation(summary = "List all roles")
    @GetMapping("/roles")
    ResponseEntity<List<RoleDetailResponse>> getAllRoles();

    @Operation(summary = "Create a new role")
    @PostMapping("/roles")
    ResponseEntity<RoleDetailResponse> createRole(@Valid @RequestBody CreateRoleRequest request);

    @Operation(summary = "Update role display name / description")
    @PatchMapping("/roles/{roleId}")
    ResponseEntity<RoleDetailResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody CreateRoleRequest request);

    @Operation(summary = "Delete a non-system role")
    @DeleteMapping("/roles/{roleId}")
    ResponseEntity<Void> deleteRole(@PathVariable Long roleId);

    @Operation(summary = "Assign permissions to a role")
    @PostMapping("/roles/{roleId}/permissions")
    ResponseEntity<RoleDetailResponse> assignPermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsRequest request);

    @Operation(summary = "Revoke a permission from a role")
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    ResponseEntity<Void> revokePermission(@PathVariable Long roleId, @PathVariable Long permissionId);

    // ── Permissions ───────────────────────────────────────────────────────────

    @Operation(summary = "List all permissions")
    @GetMapping("/permissions")
    ResponseEntity<List<PermissionResponse>> getAllPermissions();

    @Operation(summary = "Create a new permission")
    @PostMapping("/permissions")
    ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request);

    @Operation(summary = "Update permission description")
    @PatchMapping("/permissions/{permissionId}")
    ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody CreatePermissionRequest request);

    @Operation(summary = "Delete a permission")
    @DeleteMapping("/permissions/{permissionId}")
    ResponseEntity<Void> deletePermission(@PathVariable Long permissionId);
}
