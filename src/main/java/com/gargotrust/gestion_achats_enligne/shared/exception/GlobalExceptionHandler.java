package com.gargotrust.gestion_achats_enligne.shared.exception;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IamException.class)
    public ResponseEntity<ApiError> handleIamException(IamException ex) {
        int status = resolveHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(ApiError.of(ex.getErrorCode(), status));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiError.of("ERR_NOT_FOUND", 404));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(ApiError.of("ERR_INVALID_CREDENTIALS", 401));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(ApiError.of("ERR_FORBIDDEN", 403));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(400).body(ApiError.of("ERR_PROFILE_PHOTO_TOO_LARGE", 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(400).body(ApiError.of("ERR_VALIDATION", 400, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(ApiError.of("ERR_INTERNAL", 500));
    }

    private int resolveHttpStatus(String errorCode) {
        return switch (errorCode) {
            case IamException.ACCOUNT_NOT_FOUND,
                 IamException.OTP_INVALID,
                 IamException.REFRESH_TOKEN_INVALID,
                 IamException.PASSWORD_RESET_TOKEN_INVALID,
                 IamException.PROFILE_NOT_FOUND,
                 IamException.ROLE_NOT_FOUND,
                 IamException.PERMISSION_NOT_FOUND         -> 404;

            case IamException.INVALID_CREDENTIALS,
                 IamException.OTP_EXPIRED,
                 IamException.OTP_ALREADY_CONSUMED,
                 IamException.REFRESH_TOKEN_EXPIRED,
                 IamException.REFRESH_TOKEN_REVOKED,
                 IamException.PASSWORD_RESET_TOKEN_EXPIRED -> 401;

            case IamException.ACCOUNT_SUSPENDED,
                 IamException.ACCOUNT_PENDING_VERIFICATION,
                 IamException.PROFILE_ROLE_MISMATCH,
                 IamException.CANNOT_CHANGE_OWN_ROLE       -> 403;

            case IamException.ACCOUNT_ALREADY_EXISTS,
                 IamException.ROLE_ALREADY_EXISTS,
                 IamException.PERMISSION_ALREADY_EXISTS    -> 409;

            case IamException.ROLE_IS_SYSTEM,
                 IamException.PHOTO_INVALID_FORMAT,
                 IamException.PHOTO_TOO_LARGE,
                 IamException.INVALID_STATUS,
                 IamException.GOOGLE_EMAIL_NOT_VERIFIED    -> 400;

            case IamException.GOOGLE_TOKEN_INVALID         -> 401;

            default -> 500;
        };
    }
}
