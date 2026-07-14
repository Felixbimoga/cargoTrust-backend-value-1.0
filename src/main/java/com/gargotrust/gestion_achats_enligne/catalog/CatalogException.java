package com.gargotrust.gestion_achats_enligne.catalog;

import com.gargotrust.gestion_achats_enligne.shared.exception.DomainException;

public class CatalogException extends DomainException {

    public CatalogException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String CATEGORY_NOT_FOUND = "ERR_CATEGORY_NOT_FOUND";
    public static final String CATEGORY_CODE_EXISTS = "ERR_CATEGORY_CODE_EXISTS";
    public static final String CATEGORY_INVALID     = "ERR_CATEGORY_INVALID";

    public static CatalogException notFound()   { return new CatalogException(CATEGORY_NOT_FOUND, 404); }
    public static CatalogException codeExists()  { return new CatalogException(CATEGORY_CODE_EXISTS, 409); }
    public static CatalogException invalid()      { return new CatalogException(CATEGORY_INVALID, 400); }
}
