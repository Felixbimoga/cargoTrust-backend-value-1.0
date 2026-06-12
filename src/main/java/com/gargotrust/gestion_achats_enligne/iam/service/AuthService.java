package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.request.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.MessageResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.*;
import com.gargotrust.gestion_achats_enligne.iam.service.interfaces.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AccountRepository          accountRepository;
    private final RoleRepository             roleRepository;
    private final RolePermissionRepository   rolePermissionRepository;
    private final RefreshTokenRepository     refreshTokenRepository;
    private final LoginSessionRepository     loginSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService                 jwtService;
    private final OtpService                 otpService;
    private final PasswordEncoder            passwordEncoder;
    private final IProfileService            profileService;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.security.password-reset.expiration-hours:1}")
    private int passwordResetExpirationHours;

    // ─────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IamException(IamException.ACCOUNT_ALREADY_EXISTS);
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(Account.AccountStatus.PENDING_VERIFICATION)
                .build();
        accountRepository.save(account);

        Role role = roleRepository.findByName(Role.IMPORTER)
                .orElseThrow(() -> new RuntimeException("ROLE_IMPORTER not found in DB"));
        AccountRole accountRole = AccountRole.builder().account(account).role(role).build();
        account.getAccountRoles().add(accountRole);
        accountRepository.save(account);

        otpService.generateAndSaveOtp(account.getId(), account.getEmail(), OtpToken.OtpType.REGISTRATION);

        profileService.createProfileForNewAccount(account.getId());

        return new MessageResponse("Inscription réussie. Vérifiez votre email pour activer votre compte.", true);
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IamException(IamException.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            saveLoginSession(account.getId(), httpRequest, false);
            throw new IamException(IamException.INVALID_CREDENTIALS);
        }

        if (account.getStatus() == Account.AccountStatus.SUSPENDED) {
            throw new IamException(IamException.ACCOUNT_SUSPENDED);
        }
        if (account.getStatus() == Account.AccountStatus.PENDING_VERIFICATION) {
            throw new IamException(IamException.ACCOUNT_PENDING_VERIFICATION);
        }

        String roleName = extractRoleName(account);
        RefreshTokenPair pair = createRefreshToken(account.getId(), httpRequest);
        saveLoginSession(account.getId(), httpRequest, true);

        if (otpService.needsDailyOtp(pair.entity())) {
            otpService.generateAndSaveOtp(account.getId(), account.getEmail(), OtpToken.OtpType.LOGIN);
            return AuthResponse.builder()
                    .requiresOtp(true).message("Un code OTP a été envoyé à votre email.")
                    .tokenType("Bearer").build();
        }

        String accessToken = buildAccessToken(account, roleName);
        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(pair.rawToken())
                .tokenType("Bearer").expiresIn(refreshTokenExpiration / 1000).requiresOtp(false)
                .build();
    }

    // ─────────────────────────────────────────────
    // VERIFY OTP
    // ─────────────────────────────────────────────
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));

        OtpToken.OtpType otpType = OtpToken.OtpType.valueOf(request.getType().toUpperCase());
        otpService.verifyOtp(account.getId(), request.getOtpCode(), otpType);

        if (account.getStatus() == Account.AccountStatus.PENDING_VERIFICATION) {
            account.setStatus(Account.AccountStatus.ACTIVE);
            accountRepository.save(account);
        }

        refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getAccountId().equals(account.getId()) && !rt.isRevoked())
                .findFirst()
                .ifPresent(rt -> { rt.setLastOtpDate(LocalDate.now()); refreshTokenRepository.save(rt); });

        String roleName = extractRoleName(account);
        RefreshTokenPair pair = createRefreshToken(account.getId(), null);
        String accessToken = buildAccessToken(account, roleName);

        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(pair.rawToken())
                .tokenType("Bearer").expiresIn(refreshTokenExpiration / 1000).requiresOtp(false)
                .build();
    }

    // ─────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IamException(IamException.REFRESH_TOKEN_INVALID));

        if (refreshToken.isRevoked()) throw new IamException(IamException.REFRESH_TOKEN_REVOKED);
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) throw new IamException(IamException.REFRESH_TOKEN_EXPIRED);

        Account account = accountRepository.findById(refreshToken.getAccountId())
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));

        if (account.getStatus() == Account.AccountStatus.SUSPENDED) {
            throw new IamException(IamException.ACCOUNT_SUSPENDED);
        }

        String roleName = extractRoleName(account);
        String accessToken = buildAccessToken(account, roleName);

        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(request.getRefreshToken())
                .tokenType("Bearer").expiresIn(900L).requiresOtp(false)
                .build();
    }

    // ─────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────
    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // ─────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            String rawToken  = UUID.randomUUID().toString();
            String tokenHash = hashToken(rawToken);
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .accountId(account.getId()).tokenHash(tokenHash)
                    .expiresAt(Instant.now().plus(passwordResetExpirationHours, ChronoUnit.HOURS))
                    .build());
            otpService.generateAndSaveOtp(account.getId(), account.getEmail(), OtpToken.OtpType.PASSWORD_RESET);
        });
        return new MessageResponse("Si cet email existe, vous recevrez un lien de réinitialisation.", true);
    }

    // ─────────────────────────────────────────────
    // RESET PASSWORD
    // ─────────────────────────────────────────────
    @Transactional
    public MessageResponse resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenHash, Instant.now())
                .orElseThrow(() -> new IamException(IamException.PASSWORD_RESET_TOKEN_INVALID));

        Account account = accountRepository.findById(resetToken.getAccountId())
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByAccountId(account.getId());

        return new MessageResponse("Mot de passe réinitialisé avec succès.", true);
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private String extractRoleName(Account account) {
        return account.getAccountRoles().stream()
                .findFirst().map(ar -> ar.getRole().getName()).orElse(Role.IMPORTER);
    }

    private List<String> loadPermissions(Account account) {
        return account.getAccountRoles().stream()
                .findFirst()
                .map(ar -> rolePermissionRepository.findAllByRoleId(ar.getRole().getId()).stream()
                        .map(rp -> rp.getPermission().getName())
                        .toList())
                .orElse(List.of());
    }

    private String buildAccessToken(Account account, String roleName) {
        return jwtService.generateAccessToken(account, roleName, loadPermissions(account));
    }

    private record RefreshTokenPair(RefreshToken entity, String rawToken) {}

    private RefreshTokenPair createRefreshToken(UUID accountId, HttpServletRequest httpRequest) {
        String rawToken  = jwtService.generateRefreshTokenValue();
        String tokenHash = hashToken(rawToken);
        RefreshToken saved = refreshTokenRepository.save(RefreshToken.builder()
                .accountId(accountId).tokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : null)
                .build());
        return new RefreshTokenPair(saved, rawToken);
    }

    private void saveLoginSession(UUID accountId, HttpServletRequest req, boolean successful) {
        loginSessionRepository.save(LoginSession.builder()
                .accountId(accountId).ipAddress(req.getRemoteAddr())
                .userAgent(req.getHeader("User-Agent")).loginAt(Instant.now())
                .successful(successful).build());
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
