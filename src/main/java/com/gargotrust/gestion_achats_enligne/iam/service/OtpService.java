package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.OtpToken;
import com.gargotrust.gestion_achats_enligne.iam.domain.RefreshToken;
import com.gargotrust.gestion_achats_enligne.iam.repository.OtpTokenRepository;
import com.gargotrust.gestion_achats_enligne.iam.service.interfaces.IOtpService;
import com.gargotrust.gestion_achats_enligne.shared.events.OtpRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.security.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Transactional
    public String generateAndSaveOtp(UUID accountId, String email, OtpToken.OtpType type) {
        // Invalider les anciens OTP du même type
        otpTokenRepository.deleteAllByAccountIdAndType(accountId, type);

        // Générer code 6 chiffres
        String code = String.format("%06d", new Random().nextInt(999999));
        String hash = hashOtp(code);

        OtpToken token = OtpToken.builder()
                .accountId(accountId)
                .otpHash(hash)
                .type(type)
                .expiresAt(Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES))
                .build();
        otpTokenRepository.save(token);

        // Publier l'événement (le module Notification l'écoutera plus tard)
        eventPublisher.publishEvent(new OtpRequestedEvent(
                accountId, email, code, type.name(), Instant.now()
        ));

        return code;
    }

    @Transactional
    public void verifyOtp(UUID accountId, String code, OtpToken.OtpType type) {
        String hash = hashOtp(code);

        OtpToken token = otpTokenRepository
                .findByAccountIdAndTypeAndConsumedFalseAndExpiresAtAfter(accountId, type, Instant.now())
                .orElseThrow(() -> new IamException(IamException.OTP_INVALID));

        if (!token.getOtpHash().equals(hash)) {
            throw new IamException(IamException.OTP_INVALID);
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IamException(IamException.OTP_EXPIRED);
        }

        token.setConsumed(true);
        otpTokenRepository.save(token);
    }

    public boolean needsDailyOtp(RefreshToken refreshToken) {
        return refreshToken.getLastOtpDate() == null
                || refreshToken.getLastOtpDate().isBefore(LocalDate.now());
    }

    private String hashOtp(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
