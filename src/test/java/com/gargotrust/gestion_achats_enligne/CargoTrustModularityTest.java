package com.gargotrust.gestion_achats_enligne;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class CargoTrustModularityTest {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(CargoTrustApplication.class).verify();
    }
}
