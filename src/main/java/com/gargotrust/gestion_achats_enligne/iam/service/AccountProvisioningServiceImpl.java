package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.domain.AccountRole;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import com.gargotrust.gestion_achats_enligne.shared.service.AccountProvisioningService;
import com.gargotrust.gestion_achats_enligne.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Implémentation IAM du port {@link AccountProvisioningService}. Crée un compte actif
 * (aucune vérification OTP) avec mot de passe temporaire, pré-remplit le profil et
 * envoie les identifiants par email. En cas d'échec d'envoi, la transaction est annulée
 * pour éviter un compte orphelin dont personne ne connaît le mot de passe.
 */
@Service
@RequiredArgsConstructor
public class AccountProvisioningServiceImpl implements AccountProvisioningService {

    private final AccountRepository accountRepo;
    private final RoleRepository    roleRepo;
    private final IProfileService   profileService;
    private final PasswordEncoder   passwordEncoder;
    private final EmailService      emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PWD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*";

    @Override
    @Transactional
    public ProvisionedAccount provision(ProvisionAccountCommand cmd) {
        if (accountRepo.existsByEmail(cmd.email())) {
            throw new IamException(IamException.ACCOUNT_ALREADY_EXISTS);
        }

        Role role = roleRepo.findByName(normalizeRoleName(cmd.roleName()))
                .orElseThrow(() -> new IamException(IamException.ROLE_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword();
        Account account = Account.builder()
                .email(cmd.email())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .status(Account.AccountStatus.ACTIVE)
                .build();
        account.getAccountRoles().add(AccountRole.builder().account(account).role(role).build());
        accountRepo.save(account);

        profileService.createProfileForNewAccount(account.getId(), cmd.firstName(), cmd.lastName());
        emailService.sendWelcomeEmail(account.getEmail(), temporaryPassword, role.getDisplayName());

        return new ProvisionedAccount(account.getId(), account.getEmail());
    }

    private String normalizeRoleName(String raw) {
        String upper = raw.trim().toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PWD_CHARS.charAt(RANDOM.nextInt(PWD_CHARS.length())));
        }
        return sb.toString();
    }
}
