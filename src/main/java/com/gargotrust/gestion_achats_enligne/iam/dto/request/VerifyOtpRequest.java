package com.gargotrust.gestion_achats_enligne.iam.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, max = 6)
    private String otpCode;
    @NotBlank
    private String type; // REGISTRATION | LOGIN | PASSWORD_RESET
}
