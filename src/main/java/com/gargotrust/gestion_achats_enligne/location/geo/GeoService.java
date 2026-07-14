package com.gargotrust.gestion_achats_enligne.location.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.gargotrust.gestion_achats_enligne.location.LocationException;
import com.gargotrust.gestion_achats_enligne.location.geo.GeoDtos.CountryDto;
import com.gargotrust.gestion_achats_enligne.location.geo.GeoDtos.CountryStatesDto;
import com.gargotrust.gestion_achats_enligne.location.geo.GeoDtos.StateDto;
import com.gargotrust.gestion_achats_enligne.shared.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Référentiel géographique adossé à l'API countriesnow.space.
 * Résultats mis en cache (in-memory) pour éviter les appels réseau répétés.
 */
@Service
@Slf4j
public class GeoService {

    private final RestClient client;

    public GeoService(@Value("${app.geo.countriesnow.base-url:https://countriesnow.space/api/v0.1}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Cacheable(CacheConfig.GEO_COUNTRIES)
    public List<CountryDto> getCountries() {
        JsonNode data = fetch(b -> b.path("/countries/iso").build());
        List<CountryDto> out = new ArrayList<>();
        for (JsonNode n : data) {
            out.add(new CountryDto(text(n, "name"), text(n, "Iso2"), text(n, "Iso3")));
        }
        return out;
    }

    @Cacheable(CacheConfig.GEO_STATES)
    public CountryStatesDto getStates(String country) {
        JsonNode data = fetch(b -> b.path("/countries/states/q").queryParam("country", country).build());
        List<StateDto> states = new ArrayList<>();
        for (JsonNode s : data.path("states")) {
            states.add(new StateDto(text(s, "name"), text(s, "state_code")));
        }
        return new CountryStatesDto(text(data, "name"), text(data, "iso3"), text(data, "iso2"), states);
    }

    @Cacheable(cacheNames = CacheConfig.GEO_CITIES, key = "#country + '|' + (#state == null ? '' : #state)")
    public List<String> getCities(String country, String state) {
        JsonNode data = (state == null || state.isBlank())
                ? fetch(b -> b.path("/countries/cities/q").queryParam("country", country).build())
                : fetch(b -> b.path("/countries/state/cities/q")
                        .queryParam("country", country).queryParam("state", state).build());
        List<String> cities = new ArrayList<>();
        data.forEach(c -> cities.add(c.asText()));
        return cities;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JsonNode fetch(java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uri) {
        try {
            JsonNode body = client.get().uri(uri).retrieve().body(JsonNode.class);
            if (body == null || body.path("error").asBoolean(true)) {
                throw LocationException.geoUnavailable();
            }
            return body.path("data");
        } catch (LocationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Appel countriesnow en échec : {}", e.getMessage());
            throw LocationException.geoUnavailable();
        }
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
