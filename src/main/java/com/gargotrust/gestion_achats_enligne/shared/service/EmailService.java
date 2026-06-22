package com.gargotrust.gestion_achats_enligne.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "CargoTrust");
            helper.setTo(to);
            helper.setSubject(getSubject(type));
            helper.setText(getEmailBody(otpCode, type), true); // true = HTML

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
            case "REGISTRATION" -> buildHtml(
                    "Vérification de votre inscription",
                    "Merci de vous être inscrit sur CargoTrust. Utilisez le code ci-dessous pour activer votre compte.",
                    otpCode, "10 minutes");
            case "LOGIN" -> buildHtml(
                    "Code de sécurité",
                    "Voici votre code de sécurité pour vous connecter à votre compte CargoTrust.",
                    otpCode, "10 minutes");
            case "PASSWORD_RESET" -> buildHtml(
                    "Réinitialisation du mot de passe",
                    "Utilisez le code ci-dessous pour réinitialiser votre mot de passe.",
                    otpCode, "1 heure");
            default -> buildHtml(
                    "Code de vérification",
                    "Voici votre code de vérification CargoTrust.",
                    otpCode, "10 minutes");
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

    private String buildHtml(String title, String intro, String otpCode, String expiry) {
        return ("""
        <!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"></head>
        <body style="margin:0;padding:24px 12px;background:#EAEEF2;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#EAEEF2;"><tr><td align="center">
            <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="width:600px;max-width:100%%;background:#FFFFFF;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(13,27,42,.10);">
              <tr><td align="center" style="background:#0D1B2A;padding:30px 24px;">
                <div style="font-size:24px;font-weight:700;color:#FFFFFF;">Cargo<span style="color:#F77F00;">Trust</span></div>
                <div style="font-size:11px;color:#9FB0C4;letter-spacing:1.5px;text-transform:uppercase;margin-top:6px;">Plateforme d'importations s&eacute;curis&eacute;e</div>
              </td></tr>
              <tr><td style="padding:38px 40px 30px;">
                <h1 style="margin:0 0 6px;font-size:20px;font-weight:700;color:#0D1B2A;">%s</h1>
                <p style="margin:0 0 22px;font-size:15px;line-height:1.55;color:#6B7A8F;">%s</p>
                <p style="margin:0 0 10px;font-size:11px;font-weight:600;letter-spacing:1px;text-transform:uppercase;color:#94A3B8;">Votre code de v&eacute;rification</p>
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr>
                  <td align="center" style="background:#F4F6FA;border:1px solid #E9EDF4;border-top:3px solid #F77F00;border-radius:14px;padding:24px 16px;">
                    <span style="font-size:38px;font-weight:700;letter-spacing:10px;color:#0D1B2A;">%s</span>
                  </td>
                </tr></table>
                <p style="margin:22px 0 0;font-size:14px;line-height:1.55;color:#0D1B2A;">&#9201;&nbsp; Ce code expire dans <strong>%s</strong>.</p>
                <p style="margin:8px 0 0;font-size:13px;line-height:1.55;color:#6B7A8F;">Pour des raisons de s&eacute;curit&eacute;, ne le partagez avec personne. Si vous n'&ecirc;tes pas &agrave; l'origine de cette demande, ignorez cet email.</p>
                <div style="height:1px;background:#E9EDF4;margin:28px 0 20px;"></div>
                <p style="margin:0;font-size:14px;color:#6B7A8F;">Cordialement,<br><strong style="color:#0D1B2A;">L'&eacute;quipe CargoTrust</strong></p>
              </td></tr>
              <tr><td align="center" style="background:#F4F6FA;padding:20px 24px;border-top:1px solid #E9EDF4;">
                <p style="margin:0;font-size:11.5px;color:#94A3B8;">&copy; CargoTrust &middot; Email automatique, merci de ne pas y r&eacute;pondre.</p>
              </td></tr>
            </table>
          </td></tr></table>
        </body></html>
        """).formatted(title, intro, otpCode, expiry);
    }
}
