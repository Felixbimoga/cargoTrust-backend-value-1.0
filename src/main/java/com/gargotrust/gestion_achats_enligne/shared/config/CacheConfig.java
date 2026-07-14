package com.gargotrust.gestion_achats_enligne.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache in-memory (Redis désactivé). Utilisé notamment pour les données de
 * référence géographiques (countriesnow) afin d'éviter les appels réseau répétés.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GEO_COUNTRIES = "geo-countries";
    public static final String GEO_STATES    = "geo-states";
    public static final String GEO_CITIES    = "geo-cities";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(GEO_COUNTRIES, GEO_STATES, GEO_CITIES);
    }
}
