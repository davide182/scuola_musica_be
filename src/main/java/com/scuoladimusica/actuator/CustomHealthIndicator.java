package com.scuoladimusica.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // logica personalizzata qui
        // ad esempio: verifica connessione al database, spazio disco, ecc.
        
        boolean isHealthy = checkCustomCondition();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("scuola", "Scuola di Musica")
                    .withDetail("stato", "Operativa")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        } else {
            return Health.down()
                    .withDetail("errore", "Problema rilevato nel sistema")
                    .build();
        }
    }

    private boolean checkCustomCondition() {
        // la tua logica di verifica
        // Per ora restituisce sempre true
        return true;
    }
}