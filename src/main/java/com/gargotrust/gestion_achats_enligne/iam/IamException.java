package com.gargotrust.gestion_achats_enligne.iam;

import lombok.Getter;

@Getter
public class IamException extends RuntimeException {

    private final String errorCode;

    public IamException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    // ── Auth ──────────────────────────────────────────────────────────────────
    public static final String ACCOUNT_NOT_FOUND            = "ERR_ACCOUNT_NOT_FOUND";
    public static final String ACCOUNT_ALREADY_EXISTS       = "ERR_ACCOUNT_ALREADY_EXISTS";
    public static final String ACCOUNT_SUSPENDED            = "ERR_ACCOUNT_SUSPENDED";
    public static final String ACCOUNT_PENDING_VERIFICATION = "ERR_ACCOUNT_PENDING_VERIFICATION";
    public static final String INVALID_CREDENTIALS          = "ERR_INVALID_CREDENTIALS";
    public static final String OTP_INVALID                  = "ERR_OTP_INVALID";
    public static final String OTP_EXPIRED                  = "ERR_OTP_EXPIRED";
    public static final String OTP_ALREADY_CONSUMED         = "ERR_OTP_ALREADY_CONSUMED";
    public static final String REFRESH_TOKEN_INVALID        = "ERR_REFRESH_TOKEN_INVALID";
    public static final String REFRESH_TOKEN_EXPIRED        = "ERR_REFRESH_TOKEN_EXPIRED";
    public static final String REFRESH_TOKEN_REVOKED        = "ERR_REFRESH_TOKEN_REVOKED";
    public static final String PASSWORD_RESET_TOKEN_INVALID = "ERR_PASSWORD_RESET_TOKEN_INVALID";
    public static final String PASSWORD_RESET_TOKEN_EXPIRED = "ERR_PASSWORD_RESET_TOKEN_EXPIRED";

    // ── Profile ───────────────────────────────────────────────────────────────
    public static final String PROFILE_NOT_FOUND       = "ERR_PROFILE_NOT_FOUND";
    public static final String PROFILE_ROLE_MISMATCH   = "ERR_PROFILE_ROLE_MISMATCH";
    public static final String PHOTO_INVALID_FORMAT    = "ERR_PROFILE_PHOTO_INVALID";
    public static final String PHOTO_TOO_LARGE         = "ERR_PROFILE_PHOTO_TOO_LARGE";

    // ── Google OAuth2 ─────────────────────────────────────────────────────────
    public static final String GOOGLE_TOKEN_INVALID      = "ERR_GOOGLE_TOKEN_INVALID";
    public static final String GOOGLE_EMAIL_NOT_VERIFIED = "ERR_GOOGLE_EMAIL_NOT_VERIFIED";

    // ── Admin / Validation ────────────────────────────────────────────────────
    public static final String INVALID_STATUS       = "ERR_INVALID_STATUS";

    // ── RBAC / Admin ──────────────────────────────────────────────────────────
    public static final String ROLE_NOT_FOUND          = "ERR_ROLE_NOT_FOUND";
    public static final String ROLE_ALREADY_EXISTS     = "ERR_ROLE_ALREADY_EXISTS";
    public static final String ROLE_IS_SYSTEM          = "ERR_ROLE_IS_SYSTEM";
    public static final String PERMISSION_NOT_FOUND    = "ERR_PERMISSION_NOT_FOUND";
    public static final String PERMISSION_ALREADY_EXISTS = "ERR_PERMISSION_ALREADY_EXISTS";
    public static final String CANNOT_CHANGE_OWN_ROLE  = "ERR_CANNOT_CHANGE_OWN_ROLE";
}
