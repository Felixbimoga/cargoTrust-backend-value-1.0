package com.gargotrust.gestion_achats_enligne.iam.admin.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.AssignPermissionsRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreatePermissionRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.PermissionResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.RoleDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.domain.Permission;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.domain.RolePermission;
import com.gargotrust.gestion_achats_enligne.iam.repository.PermissionRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RolePermissionRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoleService implements IAdminRoleService {

    private final RoleRepository           roleRepo;
    private final PermissionRepository     permRepo;
    private final RolePermissionRepository rolePerm;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDetailResponse> getAllRoles() {
        return roleRepo.findAll().stream().map(this::toDetail).toList();
    }

    @Override
    @Transactional
    public RoleDetailResponse createRole(CreateRoleRequest req) {
        if (roleRepo.findByName(req.getName()).isPresent()) {
            throw new IamException(IamException.ROLE_ALREADY_EXISTS);
        }
        Role role = roleRepo.save(Role.builder()
                .name(req.getName()).displayName(req.getDisplayName())
                .description(req.getDescription()).system(false).build());
        return toDetail(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse updateRole(Long roleId, CreateRoleRequest req) {
        Role role = findRole(roleId);
        if (req.getDisplayName() != null) role.setDisplayName(req.getDisplayName());
        if (req.getDescription()  != null) role.setDescription(req.getDescription());
        return toDetail(roleRepo.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = findRole(roleId);
        if (role.isSystem()) throw new IamException(IamException.ROLE_IS_SYSTEM);
        roleRepo.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(Long roleId) {
        return rolePerm.findAllByRoleId(roleId).stream()
                .map(rp -> toPermResponse(rp.getPermission())).toList();
    }

    @Override
    @Transactional
    public RoleDetailResponse assignPermissions(Long roleId, AssignPermissionsRequest req) {
        Role role = findRole(roleId);
        for (Long permId : req.getPermissionIds()) {
            Permission perm = permRepo.findById(permId)
                    .orElseThrow(() -> new IamException(IamException.PERMISSION_NOT_FOUND));
            if (rolePerm.findAllByRoleId(roleId).stream()
                    .noneMatch(rp -> rp.getPermission().getId().equals(permId))) {
                rolePerm.save(RolePermission.builder().role(role).permission(perm).build());
            }
        }
        return toDetail(role);
    }

    @Override
    @Transactional
    public void revokePermission(Long roleId, Long permissionId) {
        rolePerm.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permRepo.findAll().stream().map(this::toPermResponse).toList();
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest req) {
        String name = req.getResource() + ":" + req.getAction();
        if (permRepo.existsByName(name)) throw new IamException(IamException.PERMISSION_ALREADY_EXISTS);
        return toPermResponse(permRepo.save(Permission.builder()
                .name(name).resource(req.getResource())
                .action(req.getAction()).description(req.getDescription()).build()));
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, CreatePermissionRequest req) {
        Permission p = permRepo.findById(id)
                .orElseThrow(() -> new IamException(IamException.PERMISSION_NOT_FOUND));
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        return toPermResponse(permRepo.save(p));
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        if (!permRepo.existsById(id)) throw new IamException(IamException.PERMISSION_NOT_FOUND);
        permRepo.deleteById(id);
    }

    private Role findRole(Long id) {
        return roleRepo.findById(id).orElseThrow(() -> new IamException(IamException.ROLE_NOT_FOUND));
    }

    private RoleDetailResponse toDetail(Role role) {
        List<String> perms = rolePerm.findAllByRoleId(role.getId()).stream()
                .map(rp -> rp.getPermission().getName()).toList();
        return RoleDetailResponse.builder()
                .id(role.getId()).name(role.getName()).displayName(role.getDisplayName())
                .description(role.getDescription()).system(role.isSystem()).permissions(perms).build();
    }

    private PermissionResponse toPermResponse(Permission p) {
        return PermissionResponse.builder().id(p.getId()).name(p.getName())
                .resource(p.getResource()).action(p.getAction()).description(p.getDescription()).build();
    }
}
