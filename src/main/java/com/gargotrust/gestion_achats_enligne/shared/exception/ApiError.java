package com.gargotrust.gestion_achats_enligne.shared.exception;

import java.time.Instant;

public record ApiError(
    String code,
    int httpStatus,
    Instant timestamp,
    Object details
) {
    public static ApiError of(String code, int httpStatus) {
        return new ApiError(code, httpStatus, Instant.now(), null);
    }

    public static ApiError of(String code, int httpStatus, Object details) {
        return new ApiError(code, httpStatus, Instant.now(), details);
    }
}
