package com.mdd.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.mdd.back.config.init.SafeJwtPropertyInitializer;

@SpringBootApplication
public class BackApplication {
    public static void main(String[] args) {
        // Enregistre un initialiseur défensif qui corrige une mauvaise configuration fréquente
        // (clé plate "spring.security.oauth2.resourceserver.jwt" au lieu de
        // "spring.security.oauth2.resourceserver.jwt.jwk-set-uri").
        SpringApplication app = new SpringApplication(BackApplication.class);
        app.addInitializers(new SafeJwtPropertyInitializer());
        app.run(args);
    }
}
