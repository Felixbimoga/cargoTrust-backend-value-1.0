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
                Role.builder().name(Role.IMPORTER)
                    .displayName("Importateur")
                    .description("Importateur — gestion commandes, paiements, suivi").build(),
                Role.builder().name(Role.AGENT)
                    .displayName("Agent Terrain")
                    .description("Agent terrain — scan QR, capture preuves").build(),
                Role.builder().name(Role.ADMIN_FORWARDER)
                    .displayName("Admin Transitaire")
                    .description("Admin transitaire — gestion expéditions, validations").build(),
                Role.builder().name(Role.SUPER_RESPONSIBLE)
                    .displayName("Super Responsable")
                    .description("Super Admin Responsable — accès total").build(),
                Role.builder().name(Role.SUPER_COMMERCIAL)
                    .displayName("Super Commercial")
                    .description("Super Admin Commercial — gestion transitaires").build(),
                Role.builder().name(Role.SUPER_FINANCIAL)
                    .displayName("Super Financier")
                    .description("Super Admin Financier — supervision paiements").build(),
                Role.builder().name(Role.SUPER_PACKAGE)
                    .displayName("Super Colis")
                    .description("Super Admin Colis — supervision logistique").build()
            ));
            log.info("7 rôles CargoTrust initialisés.");
        } else {
            log.info("Rôles déjà présents en base, initialisation ignorée.");
        }
    }

    private void initializeSuperAdmin() {
        if (accountRepository.existsByEmail(superAdminEmail)) {
            log.info("Compte super admin déjà existant, initialisation ignorée.");
            return;
        }

        Role superRole = roleRepository.findByName(Role.SUPER_RESPONSIBLE)
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_RESPONSIBLE introuvable"));

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
