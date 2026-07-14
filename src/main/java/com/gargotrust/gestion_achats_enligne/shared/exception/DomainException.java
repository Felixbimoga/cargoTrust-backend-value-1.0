package com.gargotrust.gestion_achats_enligne.shared.exception;

import lombok.Getter;

/**
 * Exception métier transverse : porte un code d'erreur applicatif et le statut HTTP
 * associé. Les modules (forwarder, storage, …) l'étendent ; {@link GlobalExceptionHandler}
 * la mappe de façon générique, sans que {@code shared} ne dépende des modules métier.
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;
    private final int    httpStatus;

    public DomainException(String errorCode, int httpStatus) {
        super(errorCode);
        this.errorCode  = errorCode;
        this.httpStatus = httpStatus;
    }
}
