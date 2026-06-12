package com.gargotrust.gestion_achats_enligne.iam.controller;

import com.gargotrust.gestion_achats_enligne.iam.dto.request.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.MessageResponse;
import com.gargotrust.gestion_achats_enligne.iam.service.AuthService;
import com.gargotrust.gestion_achats_enligne.iam.service.GoogleAuthService;
import com.gargotrust.gestion_achats_enligne.shared.security.CurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final AuthService        authService;
    private final GoogleAuthService  googleAuthService;
    private final CurrentUserContext currentUserContext;

    @Override
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @Override
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie.", true));
    }

    @Override
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestParam String token,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.resetPassword(token, body.get("newPassword")));
    }

    @Override
    @PostMapping("/google/token")
    public ResponseEntity<AuthResponse> googleToken(@Valid @RequestBody GoogleTokenRequest request,
                                                    HttpServletRequest httpRequest) {
        return ResponseEntity.ok(googleAuthService.loginWithIdToken(request.getIdToken(), httpRequest));
    }

    @Override
    @PostMapping("/google/callback")
    public ResponseEntity<AuthResponse> googleCallback(@Valid @RequestBody GoogleCallbackRequest request,
                                                       HttpServletRequest httpRequest) {
        return ResponseEntity.ok(googleAuthService.loginWithCode(request.getCode(), request.getRedirectUri(), httpRequest));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok(Map.of(
                "accountId", currentUserContext.getAccountId(),
                "email",     currentUserContext.getEmail(),
                "role",      currentUserContext.getRole()
        ));
    }
}
