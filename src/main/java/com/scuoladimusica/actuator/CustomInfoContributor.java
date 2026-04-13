package com.scuoladimusica.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        details.put("avvio", LocalDateTime.now().toString());
        details.put("ambiente", "Sviluppo");
        details.put("database", "H2 In-Memory");
        details.put("security", "JWT Authentication");
        
        builder.withDetail("custom", details);
    }
}