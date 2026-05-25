package com.gargotrust.gestion_achats_enligne.configuration;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @PostConstruct
    public void init() {
        logger.info("Configuration CORS chargée !");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
                logger.info("CORS configuré avec succès !");
                logger.info("Origines autorisées: http://localhost:4200");
            }
        };
    }
}
