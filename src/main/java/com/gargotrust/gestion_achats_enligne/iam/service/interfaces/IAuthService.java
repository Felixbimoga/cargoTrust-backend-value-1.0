package com.gargotrust.gestion_achats_enligne.iam.service.interfaces;

import com.gargotrust.gestion_achats_enligne.iam.dto.request.*;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.AuthResponse;
import com.gargotrust.gestion_achats_enligne.iam.dto.response.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {

    MessageResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse verifyOtp(VerifyOtpRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String rawToken);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(String token, String newPassword);
}
