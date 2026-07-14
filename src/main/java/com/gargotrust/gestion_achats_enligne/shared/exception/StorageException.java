package com.gargotrust.gestion_achats_enligne.shared.exception;

/**
 * Exception transverse du stockage de fichiers.
 */
public class StorageException extends DomainException {

    public StorageException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String INVALID_FORMAT  = "ERR_STORAGE_INVALID_FORMAT";
    public static final String FILE_TOO_LARGE  = "ERR_STORAGE_FILE_TOO_LARGE";
    public static final String STORAGE_FAILURE = "ERR_STORAGE_FAILURE";

    public static StorageException invalidFormat() { return new StorageException(INVALID_FORMAT, 400); }
    public static StorageException fileTooLarge()  { return new StorageException(FILE_TOO_LARGE, 400); }
    public static StorageException failure()       { return new StorageException(STORAGE_FAILURE, 500); }
}
