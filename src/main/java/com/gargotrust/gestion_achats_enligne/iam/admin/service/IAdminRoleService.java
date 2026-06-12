package com.gargotrust.gestion_achats_enligne.iam.admin.service;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.AssignPermissionsRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreatePermissionRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.PermissionResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.RoleDetailResponse;

import java.util.List;

public interface IAdminRoleService {
    List<RoleDetailResponse>  getAllRoles();
    RoleDetailResponse        createRole(CreateRoleRequest request);
    RoleDetailResponse        updateRole(Long roleId, CreateRoleRequest request);
    void                      deleteRole(Long roleId);
    List<PermissionResponse>  getRolePermissions(Long roleId);
    RoleDetailResponse        assignPermissions(Long roleId, AssignPermissionsRequest request);
    void                      revokePermission(Long roleId, Long permissionId);

    List<PermissionResponse>  getAllPermissions();
    PermissionResponse        createPermission(CreatePermissionRequest request);
    PermissionResponse        updatePermission(Long permissionId, CreatePermissionRequest request);
    void                      deletePermission(Long permissionId);
}
