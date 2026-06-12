package com.gargotrust.gestion_achats_enligne;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CargoTrustApplication {
    public static void main(String[] args) {
        SpringApplication.run(CargoTrustApplication.class, args);
    }
}
