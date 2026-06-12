package com.gargotrust.gestion_achats_enligne.iam.admin.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserStatusRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.UserSearchRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserDetailResponse;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.response.UserSummaryResponse;
import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.domain.AccountRole;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRoleRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements IAdminUserService {

    private final AccountRepository     accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository        roleRepo;
    private final IProfileService       profileService;
    private final CurrentUserContext    currentUser;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(UserSearchRequest req) {
        Specification<Account> spec = buildSpec(req);
        return accountRepo.findAll(spec, PageRequest.of(req.getPage(), req.getSize()))
                .map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(UUID accountId) {
        Account account = findAccount(accountId);
        return toDetail(account);
    }

    @Override
    @Transactional
    public UserDetailResponse changeStatus(UUID accountId, ChangeUserStatusRequest req) {
        Account account = findAccount(accountId);
        account.setStatus(Account.AccountStatus.valueOf(req.getStatus()));
        return toDetail(accountRepo.save(account));
    }

    @Override
    @Transactional
    public UserDetailResponse changeRole(UUID accountId, ChangeUserRoleRequest req) {
        if (accountId.equals(currentUser.getAccountId())) {
            throw new IamException(IamException.CANNOT_CHANGE_OWN_ROLE);
        }
        Account account = findAccount(accountId);
        Role newRole = roleRepo.findByName(req.getRoleName())
                .orElseThrow(() -> new IamException(IamException.ROLE_NOT_FOUND));

        accountRoleRepo.deleteAllByAccountId(accountId);
        accountRoleRepo.save(AccountRole.builder().account(account).role(newRole).build());

        return toDetail(account);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Account findAccount(UUID accountId) {
        return accountRepo.findById(accountId)
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));
    }

    private String getRoleName(Account account) {
        return account.getAccountRoles().stream()
                .findFirst()
                .map(ar -> ar.getRole().getName())
                .orElse("N/A");
    }

    private UserSummaryResponse toSummary(Account account) {
        String roleName = getRoleName(account);
        String firstName = null, lastName = null;
        boolean profileComplete = false;
        try {
            var profile = profileService.getProfileByAccountId(account.getId());
            firstName = profile.getFirstName();
            lastName  = profile.getLastName();
            profileComplete = profile.isComplete();
        } catch (Exception ignored) {}

        return UserSummaryResponse.builder()
                .id(account.getId()).email(account.getEmail())
                .status(account.getStatus().name()).role(roleName)
                .firstName(firstName).lastName(lastName)
                .profileComplete(profileComplete).createdAt(account.getCreatedAt())
                .build();
    }

    private UserDetailResponse toDetail(Account account) {
        com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse profile = null;
        try { profile = profileService.getProfileByAccountId(account.getId()); } catch (Exception ignored) {}

        return UserDetailResponse.builder()
                .id(account.getId()).email(account.getEmail())
                .status(account.getStatus().name()).role(getRoleName(account))
                .createdAt(account.getCreatedAt()).updatedAt(account.getUpdatedAt())
                .profile(profile)
                .build();
    }

    private Specification<Account> buildSpec(UserSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (req.getEmail() != null && !req.getEmail().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + req.getEmail().toLowerCase() + "%"));
            }

            if (req.getStatus() != null && !req.getStatus().isBlank()) {
                try {
                    Account.AccountStatus status = Account.AccountStatus.valueOf(req.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    throw new IamException("ERR_INVALID_STATUS");
                }
            }

            if (req.getRole() != null && !req.getRole().isBlank()) {
                // Accepte "IMPORTER" ou "ROLE_IMPORTER"
                String roleName = req.getRole().startsWith("ROLE_")
                        ? req.getRole().toUpperCase()
                        : "ROLE_" + req.getRole().toUpperCase();
                Join<Object, Object> arJoin = root.join("accountRoles");
                Join<Object, Object> roleJoin = arJoin.join("role");
                predicates.add(cb.equal(roleJoin.get("name"), roleName));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
