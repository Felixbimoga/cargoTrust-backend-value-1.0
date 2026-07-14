package com.gargotrust.gestion_achats_enligne.importer;

import com.gargotrust.gestion_achats_enligne.shared.exception.DomainException;

public class ImporterException extends DomainException {

    public ImporterException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String NOT_FOUND         = "ERR_IMPORTER_PROFILE_NOT_FOUND";
    public static final String NOT_A_CLIENT       = "ERR_IMPORTER_NOT_A_CLIENT";

    public static ImporterException notFound()   { return new ImporterException(NOT_FOUND, 404); }
    public static ImporterException notAClient()  { return new ImporterException(NOT_A_CLIENT, 403); }
}
