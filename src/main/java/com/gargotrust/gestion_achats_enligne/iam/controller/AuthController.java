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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(
        value = "/api/v1/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final AuthService        authService;
    private final GoogleAuthService  googleAuthService;
    private final CurrentUserContext currentUserContext;

    @Override
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @Override
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @Override
    @PostMapping(value = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @Override
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Override
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(new MessageResponse("Déconnexion réussie.", true));
    }

    @Override
    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @Override
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> resetPassword(@RequestParam String token,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.resetPassword(token, body.get("newPassword")));
    }

    @Override
    @PostMapping(value = "/google/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> googleToken(@Valid @RequestBody GoogleTokenRequest request,
                                                    HttpServletRequest httpRequest) {
        return ResponseEntity.ok(googleAuthService.loginWithIdToken(request.getIdToken(), httpRequest));
    }

    @Override
    @PostMapping(value = "/google/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
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
