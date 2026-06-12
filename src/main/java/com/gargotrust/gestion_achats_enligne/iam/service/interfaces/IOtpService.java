package com.gargotrust.gestion_achats_enligne.iam.service.interfaces;

import com.gargotrust.gestion_achats_enligne.iam.domain.OtpToken;
import com.gargotrust.gestion_achats_enligne.iam.domain.RefreshToken;

import java.util.UUID;

public interface IOtpService {

    String generateAndSaveOtp(UUID accountId, String email, OtpToken.OtpType type);

    void verifyOtp(UUID accountId, String code, OtpToken.OtpType type);

    boolean needsDailyOtp(RefreshToken refreshToken);
}
