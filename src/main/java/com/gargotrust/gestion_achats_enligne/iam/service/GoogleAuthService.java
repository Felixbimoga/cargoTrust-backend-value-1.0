package com.gargotrust.gestion_achats_enligne.iam.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.domain.AccountRole;
import com.gargotrust.gestion_achats_enligne.iam.domain.Role;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRoleRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RefreshTokenRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RolePermissionRepository;
import com.gargotrust.gestion_achats_enligne.iam.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class GoogleAuthService {

    private final AccountRepository      accountRepo;
    private final AccountRoleRepository  accountRoleRepo;
    private final RoleRepository         roleRepo;
    private final RolePermissionRepository rolePermRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtService             jwtService;
    private final IProfileService        profileService;

    private final String clientIdWeb;
    private final String clientSecretWeb;
    private final String clientIdAndroid;
    private final long   refreshTokenExpiration;

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(
            AccountRepository accountRepo,
            AccountRoleRepository accountRoleRepo,
            RoleRepository roleRepo,
            RolePermissionRepository rolePermRepo,
            RefreshTokenRepository refreshTokenRepo,
            JwtService jwtService,
            IProfileService profileService,
            @Value("${app.google.client-id-web}")     String clientIdWeb,
            @Value("${app.google.client-secret-web}") String clientSecretWeb,
            @Value("${app.google.client-id-android}") String clientIdAndroid,
            @Value("${app.security.jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        this.accountRepo           = accountRepo;
        this.accountRoleRepo       = accountRoleRepo;
        this.roleRepo              = roleRepo;
        this.rolePermRepo          = rolePermRepo;
        this.refreshTokenRepo      = refreshTokenRepo;
        this.jwtService            = jwtService;
        this.profileService        = profileService;
        this.clientIdWeb           = clientIdWeb;
        this.clientSecretWeb       = clientSecretWeb;
        this.clientIdAndroid       = clientIdAndroid;
        this.refreshTokenExpiration = refreshTokenExpiration;

        List<String> audiences = Arrays.asList(clientIdWeb, clientIdAndroid)
                .stream().filter(id -> id != null && !id.isBlank()).toList();

        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(audiences.isEmpty() ? List.of("__no_audience__") : audiences)
                .build();
    }

    // ── ID Token (Android + Web one-tap) ──────────────────────────────────────

    @Transactional
    public AuthResponse loginWithIdToken(String rawIdToken, HttpServletRequest httpRequest) {
        GoogleIdToken.Payload payload = verifyIdToken(rawIdToken);
        return processGooglePayload(payload, httpRequest);
    }

    // ── Authorization Code (Web redirect flow) ────────────────────────────────

    @Transactional
    public AuthResponse loginWithCode(String code, String redirectUri, HttpServletRequest httpRequest) {
        String idTokenString = exchangeCodeForIdToken(code, redirectUri);
        GoogleIdToken.Payload payload = verifyIdToken(idTokenString);
        return processGooglePayload(payload, httpRequest);
    }

    // ── Logique commune ───────────────────────────────────────────────────────

    private AuthResponse processGooglePayload(GoogleIdToken.Payload payload, HttpServletRequest httpRequest) {
        String googleId = payload.getSubject();
        String email    = payload.getEmail();

        Account account = accountRepo.findByGoogleId(googleId)
                .orElseGet(() -> findOrCreateByEmail(googleId, email, payload));

        if (account.getStatus() == Account.AccountStatus.SUSPENDED) {
            throw new IamException(IamException.ACCOUNT_SUSPENDED);
        }

        String roleName = extractRoleName(account);
        List<String> permissions = loadPermissions(account);

        String rawRefresh  = jwtService.generateRefreshTokenValue();
        String tokenHash   = hashToken(rawRefresh);

        refreshTokenRepo.save(
                com.gargotrust.gestion_achats_enligne.iam.domain.RefreshToken.builder()
                        .accountId(account.getId())
                        .tokenHash(tokenHash)
                        .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                        .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                        .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : null)
                        .build());

        String accessToken = jwtService.generateAccessToken(account, roleName, permissions);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .tokenType("Bearer")
                .expiresIn(refreshTokenExpiration / 1000)
                .requiresOtp(false)
                .build();
    }

    private Account findOrCreateByEmail(String googleId, String email, GoogleIdToken.Payload payload) {
        return accountRepo.findByEmail(email)
                .map(existing -> {
                    // Option A : lier le compte existant au googleId
                    if (existing.getGoogleId() == null) {
                        existing.setGoogleId(googleId);
                        accountRepo.save(existing);
                        log.info("Compte {} lié au Google ID {}", email, googleId);
                    }
                    return existing;
                })
                .orElseGet(() -> createGoogleAccount(googleId, email, payload));
    }

    private Account createGoogleAccount(String googleId, String email, GoogleIdToken.Payload payload) {
        Role role = roleRepo.findByName(Role.IMPORTER)
                .orElseThrow(() -> new RuntimeException("ROLE_IMPORTER not found"));

        Account account = Account.builder()
                .email(email)
                .googleId(googleId)
                .status(Account.AccountStatus.ACTIVE) // Google a déjà vérifié l'email
                .build();
        accountRepo.save(account);

        accountRoleRepo.save(AccountRole.builder().account(account).role(role).build());

        String givenName  = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");
        profileService.createProfileForNewAccount(account.getId(), givenName, familyName);

        log.info("Nouveau compte Google créé : {}", email);
        return account;
    }

    // ── Vérification ID Token ─────────────────────────────────────────────────

    private GoogleIdToken.Payload verifyIdToken(String rawIdToken) {
        try {
            GoogleIdToken idToken = verifier.verify(rawIdToken);
            if (idToken == null) {
                throw new IamException(IamException.GOOGLE_TOKEN_INVALID);
            }
            if (!Boolean.TRUE.equals(idToken.getPayload().getEmailVerified())) {
                throw new IamException(IamException.GOOGLE_EMAIL_NOT_VERIFIED);
            }
            return idToken.getPayload();
        } catch (IamException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            throw new IamException(IamException.GOOGLE_TOKEN_INVALID);
        }
    }

    // ── Échange code → ID Token ───────────────────────────────────────────────

    private String exchangeCodeForIdToken(String code, String redirectUri) {
        try {
            var response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientIdWeb,
                    clientSecretWeb,
                    code,
                    redirectUri)
                    .execute();
            String idTokenString = response.getIdToken();
            if (idTokenString == null) {
                throw new IamException(IamException.GOOGLE_TOKEN_INVALID);
            }
            return idTokenString;
        } catch (IamException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google code exchange failed: {}", e.getMessage());
            throw new IamException(IamException.GOOGLE_TOKEN_INVALID);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractRoleName(Account account) {
        return account.getAccountRoles().stream()
                .findFirst().map(ar -> ar.getRole().getName()).orElse(Role.IMPORTER);
    }

    private List<String> loadPermissions(Account account) {
        return account.getAccountRoles().stream()
                .findFirst()
                .map(ar -> rolePermRepo.findAllByRoleId(ar.getRole().getId()).stream()
                        .map(rp -> rp.getPermission().getName()).toList())
                .orElse(List.of());
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
