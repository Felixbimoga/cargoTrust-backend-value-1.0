package com.gargotrust.gestion_achats_enligne.iam.admin.service;

import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserStatusRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.UserSearchRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IAdminUserService {
    Page<UserSummaryResponse> searchUsers(UserSearchRequest request);
    UserDetailResponse getUserDetail(UUID accountId);
    UserDetailResponse changeStatus(UUID accountId, ChangeUserStatusRequest request);
    UserDetailResponse changeRole(UUID accountId, ChangeUserRoleRequest request);
}
