package com.gargotrust.gestion_achats_enligne.forwarder;

import com.gargotrust.gestion_achats_enligne.shared.exception.DomainException;

/**
 * Exceptions métier du module Forwarder. Étend {@link DomainException} pour être
 * mappée génériquement par le GlobalExceptionHandler (code + statut HTTP).
 */
public class ForwarderException extends DomainException {

    public ForwarderException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String NOT_FOUND            = "ERR_FORWARDER_NOT_FOUND";
    public static final String ALREADY_EXISTS       = "ERR_FORWARDER_ALREADY_EXISTS";
    public static final String NOT_OWNER            = "ERR_FORWARDER_NOT_OWNER";
    public static final String NOT_A_TRANSITAIRE    = "ERR_FORWARDER_NOT_A_TRANSITAIRE";
    public static final String INCOMPLETE             = "ERR_FORWARDER_INCOMPLETE";
    public static final String INVALID_STATUS         = "ERR_FORWARDER_INVALID_STATUS";
    public static final String WAREHOUSE_NOT_FOUND    = "ERR_FORWARDER_WAREHOUSE_NOT_FOUND";
    public static final String DOCUMENTS_NOT_VERIFIED = "ERR_FORWARDER_DOCUMENTS_NOT_VERIFIED";
    public static final String MEMBER_NOT_FOUND       = "ERR_FORWARDER_MEMBER_NOT_FOUND";
    public static final String CANNOT_REMOVE_OWNER    = "ERR_FORWARDER_CANNOT_REMOVE_OWNER";

    public static ForwarderException notFound()             { return new ForwarderException(NOT_FOUND, 404); }
    public static ForwarderException alreadyExists()         { return new ForwarderException(ALREADY_EXISTS, 409); }
    public static ForwarderException notOwner()              { return new ForwarderException(NOT_OWNER, 403); }
    public static ForwarderException notATransitaire()       { return new ForwarderException(NOT_A_TRANSITAIRE, 403); }
    public static ForwarderException incomplete()            { return new ForwarderException(INCOMPLETE, 400); }
    public static ForwarderException invalidStatus()         { return new ForwarderException(INVALID_STATUS, 400); }
    public static ForwarderException warehouseNotFound()     { return new ForwarderException(WAREHOUSE_NOT_FOUND, 404); }
    public static ForwarderException documentsNotVerified()  { return new ForwarderException(DOCUMENTS_NOT_VERIFIED, 400); }
    public static ForwarderException memberNotFound()        { return new ForwarderException(MEMBER_NOT_FOUND, 404); }
    public static ForwarderException cannotRemoveOwner()     { return new ForwarderException(CANNOT_REMOVE_OWNER, 400); }
}
