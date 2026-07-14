package com.gargotrust.gestion_achats_enligne.location.geo;

import java.util.List;

/** DTO de sortie du référentiel géographique renvoyé au frontend. */
public final class GeoDtos {

    private GeoDtos() {}

    public record CountryDto(String name, String iso2, String iso3) {}

    public record StateDto(String name, String stateCode) {}

    public record CountryStatesDto(String country, String iso3, String iso2, List<StateDto> states) {}
}
