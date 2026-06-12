package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.request.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.MessageResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private AccountRepository           accountRepository;
    @Mock private RoleRepository              roleRepository;
    @Mock private RolePermissionRepository    rolePermissionRepository;
    @Mock private RefreshTokenRepository      refreshTokenRepository;
    @Mock private LoginSessionRepository      loginSessionRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private JwtService                  jwtService;
    @Mock private OtpService                  otpService;
    @Mock private PasswordEncoder             passwordEncoder;
    @Mock private IProfileService             profileService;
    @Mock private HttpServletRequest          httpRequest;

    @InjectMocks
    private AuthService authService;

    private Account      testAccount;
    private Role         testRole;
    private AccountRole  testAccountRole;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testRole = Role.builder().id(1L).name(Role.IMPORTER).build();

        testAccount = Account.builder()
                .id(UUID.randomUUID()).email("test@example.com")
                .passwordHash("encodedPassword").status(Account.AccountStatus.ACTIVE)
                .build();

        testAccountRole = AccountRole.builder().account(testAccount).role(testRole).build();
        testAccount.getAccountRoles().add(testAccountRole);

        testRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID()).accountId(testAccount.getId())
                .tokenHash("hashedToken")
                .expiresAt(Instant.now().plusMillis(2592000000L))
                .revoked(false).lastOtpDate(LocalDate.now())
                .build();

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(jwtService.generateRefreshTokenValue()).thenReturn("rawToken");
        when(rolePermissionRepository.findAllByRoleId(anyLong())).thenReturn(List.of());
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("password123");

        when(accountRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.IMPORTER)).thenReturn(Optional.of(testRole));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            if (a.getId() == null) a.setId(UUID.randomUUID());
            return a;
        });

        MessageResponse resp = authService.register(req);

        assertTrue(resp.isSuccess());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(otpService).generateAndSaveOtp(any(UUID.class), eq("new@example.com"), eq(OtpToken.OtpType.REGISTRATION));
        verify(profileService).createProfileForNewAccount(any(UUID.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("password123");

        when(accountRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IamException ex = assertThrows(IamException.class, () -> authService.register(req));
        assertEquals(IamException.ACCOUNT_ALREADY_EXISTS, ex.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(otpService.needsDailyOtp(any())).thenReturn(false);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("accessToken");
        when(refreshTokenRepository.save(any())).thenReturn(testRefreshToken);

        AuthResponse resp = authService.login(req, httpRequest);

        assertEquals("accessToken", resp.getAccessToken());
        assertFalse(resp.isRequiresOtp());
        verify(loginSessionRepository).save(argThat(LoginSession::isSuccessful));
    }

    @Test
    void login_WrongPassword_ThrowsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        IamException ex = assertThrows(IamException.class, () -> authService.login(req, httpRequest));
        assertEquals(IamException.INVALID_CREDENTIALS, ex.getErrorCode());
        verify(loginSessionRepository).save(argThat(s -> !s.isSuccessful()));
    }

    @Test
    void login_AccountSuspended_ThrowsForbidden() {
        testAccount.setStatus(Account.AccountStatus.SUSPENDED);
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenReturn(testRefreshToken);

        IamException ex = assertThrows(IamException.class, () -> authService.login(req, httpRequest));
        assertEquals(IamException.ACCOUNT_SUSPENDED, ex.getErrorCode());
    }

    @Test
    void login_PendingVerification_ThrowsForbidden() {
        testAccount.setStatus(Account.AccountStatus.PENDING_VERIFICATION);
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenReturn(testRefreshToken);

        IamException ex = assertThrows(IamException.class, () -> authService.login(req, httpRequest));
        assertEquals(IamException.ACCOUNT_PENDING_VERIFICATION, ex.getErrorCode());
    }

    @Test
    void login_RequiresOtp_ReturnsOtpChallenge() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(refreshTokenRepository.save(any())).thenReturn(testRefreshToken);
        when(otpService.needsDailyOtp(any())).thenReturn(true);

        AuthResponse resp = authService.login(req, httpRequest);

        assertTrue(resp.isRequiresOtp());
        assertNull(resp.getAccessToken());
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    @Test
    void verifyOtp_ActivatesAccount_AndReturnsTokens() {
        testAccount.setStatus(Account.AccountStatus.PENDING_VERIFICATION);
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("test@example.com");
        req.setOtpCode("123456");
        req.setType("REGISTRATION");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testAccount));
        when(refreshTokenRepository.findAll()).thenReturn(List.of(testRefreshToken));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("accessToken");
        when(refreshTokenRepository.save(any())).thenReturn(testRefreshToken);

        AuthResponse resp = authService.verifyOtp(req);

        assertEquals(Account.AccountStatus.ACTIVE, testAccount.getStatus());
        assertEquals("accessToken", resp.getAccessToken());
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rawToken");

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(testRefreshToken));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(otpService.needsDailyOtp(testRefreshToken)).thenReturn(false);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("newAccessToken");

        AuthResponse resp = authService.refreshToken(req);

        assertEquals("newAccessToken", resp.getAccessToken());
        assertFalse(resp.isRequiresOtp());
    }

    @Test
    void refreshToken_RevokedToken_ThrowsUnauthorized() {
        testRefreshToken.setRevoked(true);
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(testRefreshToken));

        IamException ex = assertThrows(IamException.class, () -> authService.refreshToken(req));
        assertEquals(IamException.REFRESH_TOKEN_REVOKED, ex.getErrorCode());
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsUnauthorized() {
        testRefreshToken.setExpiresAt(Instant.now().minusMillis(1000));
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("token");

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(testRefreshToken));

        IamException ex = assertThrows(IamException.class, () -> authService.refreshToken(req));
        assertEquals(IamException.REFRESH_TOKEN_EXPIRED, ex.getErrorCode());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_RevokesToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(testRefreshToken));

        authService.logout("rawToken");

        verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
    }

    // ── Forgot / Reset Password ───────────────────────────────────────────────

    @Test
    void forgotPassword_AlwaysReturnsOkMessage() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("anyone@example.com");

        when(accountRepository.findByEmail("anyone@example.com")).thenReturn(Optional.empty());

        MessageResponse resp = authService.forgotPassword(req);

        assertTrue(resp.isSuccess());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_InvalidToken_ThrowsNotFound() {
        when(passwordResetTokenRepository.findByTokenHashAndUsedFalseAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.empty());

        IamException ex = assertThrows(IamException.class,
                () -> authService.resetPassword("bad", "newPass"));
        assertEquals(IamException.PASSWORD_RESET_TOKEN_INVALID, ex.getErrorCode());
    }

    @Test
    void resetPassword_Success_RevokesAllTokens() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .accountId(testAccount.getId())
                .tokenHash("hash")
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false).build();

        when(passwordResetTokenRepository.findByTokenHashAndUsedFalseAndExpiresAtAfter(any(), any()))
                .thenReturn(Optional.of(resetToken));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");

        MessageResponse resp = authService.resetPassword("raw", "newPass");

        assertTrue(resp.isSuccess());
        verify(refreshTokenRepository).revokeAllByAccountId(testAccount.getId());
    }
}
