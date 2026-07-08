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

    @Value("${app.admin-transitaire.email}")
    private String adminTransitaireEmail;

    @Value("${app.transitaire.email}")
    private String transitaireEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Value("${app.admin-transitaire.password}")
    private String adminTransitairePassword;

    @Value("${app.transitaire.password}")
    private String transitairePassword;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeSuperAdmin();
        initializeAdminTransitaire();
        initializeTransitaire();
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

    private void initializeAdminTransitaire() {

        if (accountRepository.existsByEmail(adminTransitaireEmail)) {
            log.info("Compte admin transitaire déjà existant, initialisation ignorée.");
            return;
        }

        Role role = roleRepository.findByName(Role.ADMIN_TRANSITAIRE)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN_TRANSITAIRE introuvable"));

        Account adminTransitaire = Account.builder()
                .email(adminTransitaireEmail)
                .passwordHash(passwordEncoder.encode(adminTransitairePassword))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        AccountRole accountRole = AccountRole.builder()
                .account(adminTransitaire)
                .role(role)
                .build();

        adminTransitaire.getAccountRoles().add(accountRole);

        accountRepository.save(adminTransitaire);

        profileService.createProfileForNewAccount(adminTransitaire.getId());

        log.info("Compte admin transitaire créé : {}", adminTransitaireEmail);
    }

    private void initializeTransitaire() {

        if (accountRepository.existsByEmail(transitaireEmail)) {
            log.info("Compte transitaire déjà existant, initialisation ignorée.");
            return;
        }

        Role role = roleRepository.findByName(Role.TRANSITAIRE)
                .orElseThrow(() -> new IllegalStateException("ROLE_TRANSITAIRE introuvable"));

        Account transitaire = Account.builder()
                .email(transitaireEmail)
                .passwordHash(passwordEncoder.encode(transitairePassword))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        AccountRole accountRole = AccountRole.builder()
                .account(transitaire)
                .role(role)
                .build();

        transitaire.getAccountRoles().add(accountRole);

        accountRepository.save(transitaire);

        profileService.createProfileForNewAccount(transitaire.getId());

        log.info("Compte transitaire créé : {}", transitaireEmail);
    }
}
