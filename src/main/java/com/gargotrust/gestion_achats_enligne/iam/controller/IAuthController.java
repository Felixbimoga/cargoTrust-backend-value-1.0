package com.gargotrust.gestion_achats_enligne.iam.controller;

import com.gargotrust.gestion_achats_enligne.iam.dto.request.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "API de gestion de l'authentification")
public interface IAuthController {

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur", description = "Crée un compte utilisateur avec envoi d'OTP de vérification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compte créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Email déjà utilisé")
    })
    ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie l'utilisateur et retourne les tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie"),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "403", description = "Compte suspendu ou en attente de vérification")
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                         HttpServletRequest httpRequest);

    @PostMapping("/verify-otp")
    @Operation(summary = "Vérification OTP", description = "Vérifie le code OTP et active le compte si nécessaire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP vérifié avec succès"),
            @ApiResponse(responseCode = "400", description = "OTP invalide ou expiré")
    })
    ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request);

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès", description = "Génère un nouveau access token à partir du refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré")
    })
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoque le refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie")
    })
    ResponseEntity<MessageResponse> logout(@RequestBody RefreshTokenRequest request);

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de réinitialisation du mot de passe", description = "Envoie un email avec le lien de réinitialisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email envoyé si le compte existe")
    })
    ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe", description = "Change le mot de passe avec le token reçu par email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    })
    ResponseEntity<MessageResponse> resetPassword(@Parameter(description = "Token de réinitialisation") @RequestParam String token,
                                                   @RequestBody Map<String, String> body);

    @PostMapping("/google/token")
    @Operation(
        summary = "Connexion Google — ID Token",
        description = "Authentifie via un ID Token Google (Android SDK ou Web one-tap). " +
                      "Si l'email existe déjà avec un compte classique, les deux sont fusionnés (Option A).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion Google réussie"),
            @ApiResponse(responseCode = "401", description = "ID Token invalide ou expiré")
    })
    ResponseEntity<AuthResponse> googleToken(@Valid @RequestBody GoogleTokenRequest request,
                                             HttpServletRequest httpRequest);

    @PostMapping("/google/callback")
    @Operation(
        summary = "Connexion Google — Code d'autorisation",
        description = "Échange un code d'autorisation OAuth2 (Web redirect flow) contre des tokens CargoTrust.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion Google réussie"),
            @ApiResponse(responseCode = "401", description = "Code invalide ou expiré")
    })
    ResponseEntity<AuthResponse> googleCallback(@Valid @RequestBody GoogleCallbackRequest request,
                                                HttpServletRequest httpRequest);

    @GetMapping("/me")
    @Operation(summary = "Informations de l'utilisateur connecté", description = "Retourne les informations du compte authentifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informations récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    ResponseEntity<?> me();
}
