package com.gargotrust.gestion_achats_enligne.iam.admin.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserRoleRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.ChangeUserStatusRequest;
import com.gargotrust.gestion_achats_enligne.iam.admin.dto.request.CreateUserRequest;
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
import com.gargotrust.gestion_achats_enligne.shared.service.EmailService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements IAdminUserService {

    private final AccountRepository     accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository        roleRepo;
    private final IProfileService       profileService;
    private final CurrentUserContext    currentUser;
    private final PasswordEncoder       passwordEncoder;
    private final EmailService          emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PWD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*";

    /** Rôles qu'un ADMIN_TRANSITAIRE est autorisé à créer (anti-escalade de privilèges). */
    private static final Set<String> ADMIN_TRANSITAIRE_CREATABLE_ROLES =
            Set.of(Role.TRANSITAIRE, Role.CLIENT);

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
    public UserDetailResponse createUser(CreateUserRequest req) {
        if (accountRepo.existsByEmail(req.getEmail())) {
            throw new IamException(IamException.ACCOUNT_ALREADY_EXISTS);
        }

        String targetRoleName = normalizeRoleName(req.getRoleName());
        Role role = roleRepo.findByName(targetRoleName)
                .orElseThrow(() -> new IamException(IamException.ROLE_NOT_FOUND));

        enforceCreationRights(targetRoleName);

        // Compte directement actif — aucune vérification OTP requise.
        String temporaryPassword = generateTemporaryPassword();
        Account account = Account.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        AccountRole accountRole = AccountRole.builder().account(account).role(role).build();
        account.getAccountRoles().add(accountRole);
        accountRepo.save(account);

        profileService.createProfileForNewAccount(account.getId(), req.getFirstName(), req.getLastName());

        // Envoi du mot de passe + message de bienvenue. En cas d'échec, la transaction est annulée
        // pour éviter un compte orphelin dont personne ne connaît le mot de passe.
        emailService.sendWelcomeEmail(account.getEmail(), temporaryPassword, role.getDisplayName());

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

    private String normalizeRoleName(String raw) {
        String upper = raw.trim().toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    /** Empêche un ADMIN_TRANSITAIRE de créer un compte de niveau égal ou supérieur. */
    private void enforceCreationRights(String targetRoleName) {
        String creatorRole = currentUser.getRole();
        if (Role.ADMIN_TRANSITAIRE.equals(creatorRole)
                && !ADMIN_TRANSITAIRE_CREATABLE_ROLES.contains(targetRoleName)) {
            throw new IamException(IamException.CANNOT_CREATE_ROLE);
        }
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PWD_CHARS.charAt(RANDOM.nextInt(PWD_CHARS.length())));
        }
        return sb.toString();
    }

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
                // Accepte "CLIENT" ou "ROLE_CLIENT"
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
