package com.gargotrust.gestion_achats_enligne.iam.config;

import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.domain.AccountRole;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository    roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder   passwordEncoder;
    private final IProfileService   profileService;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeSuperAdmin();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                Role.builder().name(Role.SUPER_ADMIN)
                    .displayName("Super Admin")
                    .description("Super Admin — accès total au système").build(),
                Role.builder().name(Role.ADMIN_TRANSITAIRE)
                    .displayName("Admin Transitaire")
                    .description("Admin transitaire — gestion expéditions, validations").build(),
                Role.builder().name(Role.TRANSITAIRE)
                    .displayName("Transitaire")
                    .description("Transitaire — opérations terrain, scan QR, preuves").build(),
                Role.builder().name(Role.CLIENT)
                    .displayName("Client")
                    .description("Client (importateur) — commandes, paiements, suivi").build()
            ));
            log.info("4 rôles CargoTrust initialisés.");
        } else {
            log.info("Rôles déjà présents en base, initialisation ignorée.");
        }
    }

    private void initializeSuperAdmin() {
        if (accountRepository.existsByEmail(superAdminEmail)) {
            log.info("Compte super admin déjà existant, initialisation ignorée.");
            return;
        }

        Role superRole = roleRepository.findByName(Role.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN introuvable"));

        Account admin = Account.builder()
                .email(superAdminEmail)
                .passwordHash(passwordEncoder.encode(superAdminPassword))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        AccountRole adminRole = AccountRole.builder().account(admin).role(superRole).build();
        admin.getAccountRoles().add(adminRole);
        accountRepository.save(admin);

        profileService.createProfileForNewAccount(admin.getId());

        log.info("Compte super admin créé : {}", superAdminEmail);
    }
}
