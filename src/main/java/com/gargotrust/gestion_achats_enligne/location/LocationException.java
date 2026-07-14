package com.gargotrust.gestion_achats_enligne.location;

import com.gargotrust.gestion_achats_enligne.shared.exception.DomainException;

public class LocationException extends DomainException {

    public LocationException(String errorCode, int httpStatus) {
        super(errorCode, httpStatus);
    }

    public static final String ADDRESS_NOT_FOUND = "ERR_ADDRESS_NOT_FOUND";
    public static final String GEO_UNAVAILABLE    = "ERR_GEO_UNAVAILABLE";

    public static LocationException addressNotFound() { return new LocationException(ADDRESS_NOT_FOUND, 404); }
    public static LocationException geoUnavailable()  { return new LocationException(GEO_UNAVAILABLE, 502); }
}
