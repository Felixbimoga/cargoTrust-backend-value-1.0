package com.gargotrust.gestion_achats_enligne.document;

import com.gargotrust.gestion_achats_enligne.shared.exception.DomainException;

public class DocumentException extends DomainException {

    public DocumentException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String NOT_FOUND = "ERR_DOCUMENT_NOT_FOUND";

    public static DocumentException notFound() { return new DocumentException(NOT_FOUND, 404); }
}
