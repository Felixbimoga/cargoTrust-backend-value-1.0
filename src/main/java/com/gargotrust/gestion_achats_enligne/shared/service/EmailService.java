package com.gargotrust.gestion_achats_enligne.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otpCode, String type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(getSubject(type));
            message.setText(getEmailBody(otpCode, type));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("CargoTrust - Réinitialisation de votre mot de passe");
            message.setText(getPasswordResetBody(resetLink));

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String getSubject(String type) {
        return switch (type.toUpperCase()) {
            case "REGISTRATION" -> "CargoTrust - Code de vérification pour votre inscription";
            case "LOGIN" -> "CargoTrust - Code de sécurité pour votre connexion";
            case "PASSWORD_RESET" -> "CargoTrust - Code de réinitialisation de mot de passe";
            default -> "CargoTrust - Code de vérification";
        };
    }

    private String getEmailBody(String otpCode, String type) {
        return switch (type.toUpperCase()) {
            case "REGISTRATION" -> """
                Bonjour,
                
                Merci de vous être inscrit sur CargoTrust.
                
                Votre code de vérification est : %s
                
                Ce code expire dans 10 minutes.
                
                Si vous n'avez pas demandé cette inscription, ignorez cet email.
                
                Cordialement,
                L'équipe CargoTrust
                """.formatted(otpCode);
            case "LOGIN" -> """
                Bonjour,
                
                Votre code de sécurité pour la connexion est : %s
                
                Ce code expire dans 10 minutes.
                
                Si vous n'avez pas tenté de vous connecter, ignorez cet email.
                
                Cordialement,
                L'équipe CargoTrust
                """.formatted(otpCode);
            case "PASSWORD_RESET" -> """
                Bonjour,
                
                Votre code de réinitialisation de mot de passe est : %s
                
                Ce code expire dans 1 heure.
                
                Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                
                Cordialement,
                L'équipe CargoTrust
                """.formatted(otpCode);
            default -> """
                Bonjour,
                
                Votre code de vérification est : %s
                
                Ce code expire dans 10 minutes.
                
                Cordialement,
                L'équipe CargoTrust
                """.formatted(otpCode);
        };
    }

    private String getPasswordResetBody(String resetLink) {
        return """
            Bonjour,
            
            Vous avez demandé la réinitialisation de votre mot de passe.
            
            Cliquez sur le lien suivant pour réinitialiser votre mot de passe :
            %s
            
            Ce lien expire dans 1 heure.
            
            Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
            
            Cordialement,
            L'équipe CargoTrust
            """.formatted(resetLink);
    }
}
